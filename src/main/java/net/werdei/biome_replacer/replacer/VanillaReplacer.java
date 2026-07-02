package net.werdei.biome_replacer.replacer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.config.Config;
//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
//?}

import java.util.*;
import java.util.stream.Collectors;

import static net.werdei.biome_replacer.BiomeReplacer.log;

public class VanillaReplacer
{
    private static Map<Holder<Biome>, Holder<Biome>> replacementRules = Map.of();
    private static Map<String, Map<Holder<Biome>, Holder<Biome>>> mergedDimensionRules = Map.of();

    public static void doReplacement(Registry<LevelStem> stemRegistry)
    {
        if (replacementRules.isEmpty() && mergedDimensionRules.isEmpty())
        {
            BiomeReplacer.log("No rules found, not replacing anything");
            return;
        }

        for (var levelStem : stemRegistry.entrySet())
        {
            var levelId = levelStem.getKey().identifier();
            var level = levelStem.getValue();

            //? if biolith {
            if (BiolithReplacer.claims(level.type()))
            {
                BiomeReplacer.log(levelId + " is managed through Biolith, leaving its noise parameters untouched");
                continue;
            }
            //?}

            if (!(level.generator() instanceof NoiseBasedChunkGenerator generator)
                    || !(generator.getBiomeSource() instanceof MultiNoiseBiomeSource))
            {
                // We only manipulate noise parameters, every other generator gets skipped
                BiomeReplacer.log("Skipping " + levelId);
                continue;
            }
            var effectiveRules = effectiveRules(levelId.toString());
            if (effectiveRules.isEmpty())
            {
                BiomeReplacer.log("No rules apply to " + levelId + ", leaving it untouched");
                continue;
            }

            var biomeSource = (MultiNoiseBiomeSourceExtension) generator.getBiomeSource();
            var parameters = biomeSource.biome_replacer$getParameters();

            List<Pair<Climate.ParameterPoint, Holder<Biome>>> newParameterList = new ArrayList<>();
            var changed = false;

            for (var value : parameters.values())
            {
                var newBiome = replaceFromMap(value.getSecond(), effectiveRules);
                if (newBiome != value.getSecond())
                    changed = true;
                if (newBiome == null) continue;
                newParameterList.add(new Pair<>(value.getFirst(), newBiome));
            }

            // Don't replace parameter lists when no rules matched.
            if (!changed)
            {
                BiomeReplacer.log("No rules matched any biomes in " + levelId + ", leaving it untouched");
                continue;
            }
            if (newParameterList.isEmpty())
            {
                BiomeReplacer.logWarn("Rules would remove every biome in " + levelId + ", which is not possible. Leaving it untouched");
                continue;
            }

            biomeSource.biome_replacer$setParameters(new Climate.ParameterList<>(newParameterList));

            BiomeReplacer.log("Successfully replaced biomes in " + levelId);

        }
    }

    public static void prepareRules(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        var knownDimensions = stemRegistry.entrySet().stream()
                .map(steam -> steam.getKey().identifier().toString())
                .collect(Collectors.toSet());

        var globalRules = new HashMap<Holder<Biome>, Holder<Biome>>();
        var perDimensionRules = new HashMap<String, HashMap<Holder<Biome>, Holder<Biome>>>();
        var rulesDirect = 0;
        var rulesTag = 0;
        var rulesIgnored = 0;
        
        for (var rule : Config.rules) try
        {
            var targetMap = globalRules;
            if (rule.dimension() != null)
            {
                // To catch invalid IDs and vanilla ones not starting with "minecraft:"
                var dimensionId = Identifier.tryParse(rule.dimension());
                if (dimensionId == null)
                    throw new Exception(String.format("Invalid dimension ID '%s'", rule.dimension()));
                if (!knownDimensions.contains(dimensionId.toString()))
                    throw new Exception(String.format("Dimension '%s' does not exist", dimensionId));
                //noinspection unused
                targetMap = perDimensionRules.computeIfAbsent(dimensionId.toString(), s -> new HashMap<>());
            }
            
            if (rule.from().startsWith("#"))
            {
                var tagKey = getBiomeTagKey(rule.from().substring(1));
                var newBiome = getBiomeHolder(rule.to(), biomeRegistry);
                // Unwrapping the biome key and adding all biomes from it.
                // Direct rules have priority. containsKey also preserves null removal rules.
                for (var oldBiome : biomeRegistry.getTagOrEmpty(tagKey))
                    if (!targetMap.containsKey(oldBiome))
                        targetMap.put(oldBiome, newBiome);
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

        // Cache merged rules for Blueprint's biome lookup.
        var merged = new HashMap<String, Map<Holder<Biome>, Holder<Biome>>>();
        for (var entry : perDimensionRules.entrySet())
        {
            var dimensionMerged = new HashMap<>(globalRules);
            dimensionMerged.putAll(entry.getValue());
            merged.put(entry.getKey(), dimensionMerged);
        }

        replacementRules = globalRules;
        mergedDimensionRules = merged;

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
        //var holder = registry.getHolder(resourceKey);
        
        if (holder.isPresent()) return holder.get();
        throw new Exception(String.format("Biome '%s' does not exist", id));
    }
    
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
        var resourceLocation = Identifier.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID '%s'", id));
        //? if >=1.19.3
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        var resourceLocation = Identifier.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag '#%s'", id));
        //? if >=1.19.3
        return TagKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    static Map<Holder<Biome>, Holder<Biome>> effectiveRules(String dimensionId)
    {
        return mergedDimensionRules.getOrDefault(dimensionId, replacementRules);
    }

    static boolean hasDimensionRules(String dimensionId)
    {
        return mergedDimensionRules.containsKey(dimensionId);
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        return replaceIfNeeded(original, null);
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original, String dimensionId)
    {
        if (dimensionId != null)
        {
            var dimensionRules = mergedDimensionRules.get(dimensionId);
            if (dimensionRules != null)
                return replaceFromMap(original, dimensionRules);
        }
        return replaceFromMap(original, replacementRules);
    }
    
    private static Holder<Biome> replaceFromMap(Holder<Biome> original, Map<Holder<Biome>, Holder<Biome>> rules)
    {
        if (rules == null || rules.isEmpty()) return original;
        return rules.getOrDefault(original, original);
    }
}
