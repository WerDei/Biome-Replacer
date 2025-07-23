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
//? if >=1.19.4 {
import net.minecraft.core.registries.Registries;
//?}

import java.util.*;

import static net.werdei.biome_replacer.BiomeReplacer.log;

public class VanillaReplacer
{
    private static Map<Holder<Biome>, Holder<Biome>> replacementRules;
    
    public static void doReplacement(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        PrepareRules(biomeRegistry);
        if (replacementRules.isEmpty())
        {
            BiomeReplacer.log("No rules found, not replacing anything");
            return;
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
            for (var value : parameters.values())
            {
                var newBiome = replaceIfNeeded(value.getSecond());
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
        var rulesDirect = 0;
        var rulesTag = 0;
        var rulesIgnored = 0;
        
        for (var rule : Config.rules) try
        {
            if (rule.from().startsWith("#"))
            {
                var tagKey = getBiomeTagKey(rule.from().substring(1));
                var newBiome = getBiomeHolder(rule.to(), biomeRegistry);
                // Unwrapping the biome key and adding all biomes from it.
                // Using "putIfAbsent" to make sure direct rules have priority
                for (var oldBiome : biomeRegistry.getTagOrEmpty(tagKey))
                    replacementRules.putIfAbsent(oldBiome, newBiome);
                rulesTag++;
            }
            else
            {
                var oldBiome = getBiomeHolder(rule.from(), biomeRegistry);
                var newBiome = getBiomeHolder(rule.to(), biomeRegistry);
                replacementRules.put(oldBiome, newBiome);
                rulesDirect++;
            }
        }
        catch (Exception e)
        {
            BiomeReplacer.logRuleWarning(rule.line(), e.getMessage());
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
        throw new Exception(String.format("Biome does not exist: %s", id));
    }
    
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
        var resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID: %s", id));
        //? if >=1.19.3
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        var resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag: #%s", id));
        //? if >=1.19.3
        return TagKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.3
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        return replacementRules.getOrDefault(original, original);
    }
}
