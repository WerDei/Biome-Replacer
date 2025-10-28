package net.werdei.biome_replacer.replacer;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.config.Config;
import net.werdei.biome_replacer.mixin.MultiNoiseBiomeSourceAccessor;
//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
//?}

import java.util.*;

import static net.werdei.biome_replacer.BiomeReplacer.log;

public class VanillaReplacer
{
    private static Map<Holder<Biome>, Holder<Biome>> replacementRules;
    private static Map<String, Map<Holder<Biome>, Holder<Biome>>> dimensionReplacementRules;
    
    public static void doReplacement(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        PrepareRules(biomeRegistry);
        if (replacementRules.isEmpty() && (dimensionReplacementRules == null || dimensionReplacementRules.isEmpty()))
        {
            BiomeReplacer.log("No rules found, not replacing anything");
            return;
        }
        
        var knownDimensions = new HashSet<String>();
        for (var entry : stemRegistry.entrySet())
            knownDimensions.add(entry.getKey().location().toString());
        
        if (dimensionReplacementRules != null && !dimensionReplacementRules.isEmpty())
        {
            for (var dimId : dimensionReplacementRules.keySet())
            {
                if (!knownDimensions.contains(dimId))
                {
                    for (var rule : Config.rules)
                    {
                        if (dimId.equals(rule.dimension()))
                            BiomeReplacer.logRuleWarning(rule.line(), String.format("Dimension '%s' does not exist, ignoring rule", dimId));
                    }
                }
            }
        }
        
        for (var levelStem : stemRegistry.entrySet())
        {
            var levelId = levelStem.getKey().location();
            var level = levelStem.getValue();
            
            if (!(level.generator() instanceof NoiseBasedChunkGenerator generator)
                    || !(generator.getBiomeSource() instanceof MultiNoiseBiomeSource))
            {
                // We only manipulate noise parameters, every other generator gets skipped
                BiomeReplacer.log("Skipping " + levelId);
                continue;
            }
            
            var biomeSource = (MultiNoiseBiomeSourceAccessor) generator.getBiomeSource();
            
            //? if >=1.19.4
            var parameters = biomeSource.getParameters().map((p) -> p, (holder) -> holder.value().parameters());
            //? if <1.19.4
            /*var parameters = biomeSource.getParameters();*/
            
            List<Pair<Climate.ParameterPoint, Holder<Biome>>> newParameterList = new ArrayList<>();
            
            Map<Holder<Biome>, Holder<Biome>> effectiveRules;
            {
                var merged = new HashMap<Holder<Biome>, Holder<Biome>>();
                if (replacementRules != null && !replacementRules.isEmpty())
                    merged.putAll(replacementRules);
                var dimOverlay = (dimensionReplacementRules != null)
                        ? dimensionReplacementRules.get(levelId.toString())
                        : null;
                if (dimOverlay != null && !dimOverlay.isEmpty())
                    merged.putAll(dimOverlay);
                effectiveRules = merged;
            }
            for (var value : parameters.values())
            {
                var newBiome = replaceFromMap(value.getSecond(), effectiveRules);
                if (newBiome == null) continue;
                newParameterList.add(new Pair<>(value.getFirst(), newBiome));
            }
            
            //? if >=1.19.4
            biomeSource.setParameters(Either.left(new Climate.ParameterList<>(newParameterList)));
            //? if <1.19.4
            /*biomeSource.setParameters(new Climate.ParameterList<>(newParameterList));*/
            
            BiomeReplacer.log("Successfully replaced biomes in " + levelId);
            
        }
    }
    
    private static void PrepareRules(Registry<Biome> biomeRegistry)
    {
        replacementRules = new HashMap<>();
        dimensionReplacementRules = new HashMap<>();
        var rulesDirect = 0;
        var rulesTag = 0;
        var rulesIgnored = 0;
        
        for (var rule : Config.rules) try
        {
            Map<Holder<Biome>, Holder<Biome>> targetMap = rule.dimension() == null
                    ? replacementRules
                    : dimensionReplacementRules.computeIfAbsent(rule.dimension(), k -> new HashMap<>());
            if (rule.from().startsWith("#"))
            {
                var tagKey = getBiomeTagKey(rule.from().substring(1));
                var newBiome = getBiomeHolder(rule.to(), biomeRegistry);
                // Unwrapping the biome key and adding all biomes from it.
                // Using "putIfAbsent" to make sure direct rules have priority
                for (var oldBiome : biomeRegistry.getTagOrEmpty(tagKey))
                    targetMap.putIfAbsent(oldBiome, newBiome);
                rulesTag++;
            }
            else
            {
                var oldBiome = getBiomeHolder(rule.from(), biomeRegistry);
                var newBiome = getBiomeHolder(rule.to(), biomeRegistry);
                targetMap.put(oldBiome, newBiome);
                rulesDirect++;
            }
        }
        catch (Exception e)
        {
            BiomeReplacer.logRuleWarning(rule.line(), e.getMessage() + ", ignoring rule");
            rulesIgnored++;
        }
        
        log(String.format("Loaded %d rules (%d direct, %d tag-based) and ignored %d",
                rulesDirect + rulesTag, rulesDirect, rulesTag, rulesIgnored));
    }
    
    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry) throws Exception
    {
        if (id.equals(Config.REMOVE_BIOME_KEYWORD))
            return null;
        
        var resourceKey = getBiomeResourceKey(id);
        
        //? if >=1.21.2
        var holder = registry.get(resourceKey);
        //? if <1.21.2
        /*var holder = registry.getHolder(resourceKey);*/
        
        if (holder.isPresent()) return holder.get();
        throw new Exception(String.format("Biome '%s' does not exist", id));
    }
    
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
        var resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID '%s'", id));
        //? if >=1.19.3
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        var resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag '#%s'", id));
        //? if >=1.19.3
        return TagKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        return replaceIfNeeded(original, null);
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original, String dimensionId)
    {
        var result = replaceFromMap(original, replacementRules);

        if (dimensionId != null && dimensionReplacementRules != null && !dimensionReplacementRules.isEmpty())
        {
            var dimensionRules = dimensionReplacementRules.get(dimensionId);
            if (dimensionRules != null)
                result = replaceFromMap(result, dimensionRules);
        }

        return result;
    }
    
    private static Holder<Biome> replaceFromMap(Holder<Biome> original, Map<Holder<Biome>, Holder<Biome>> rules)
    {
        if (rules == null || rules.isEmpty()) return original;
        return rules.getOrDefault(original, original);
    }
}
