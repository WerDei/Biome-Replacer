package net.werdei.biome_replacer.replacer;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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

import java.util.*;

import static net.werdei.biome_replacer.BiomeReplacer.log;
import static net.werdei.biome_replacer.BiomeReplacer.logWarn;

public class BuiltInReplacer
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
        
        // Direct biome replacements
        // Because these are loaded first and rules are added "putIfAbsent", they have precedence over tag rules.
        for (var entry : Config.rules.entrySet())
        {
            var oldBiomeId = entry.getKey();
            var replacements = entry.getValue();
            
            try
            {
                var oldBiome = getBiomeHolder(oldBiomeId, biomeRegistry);
                for (Config.BiomeReplacement replacement : replacements)
                {
                    Holder<Biome> newBiome = getBiomeHolder(replacement.targetBiome, biomeRegistry);
                    replacementRules.putIfAbsent(oldBiome, newBiome);
                }
                rulesDirect++;
            }
            catch (Exception e) {
                logWarn(String.format("Ignoring rule \"%s\" - %s", oldBiomeId, e.getMessage()));
                rulesIgnored++;
            }
        }
        
        // Tag-based replacements
        for (var entry : Config.tagRules.entrySet()) {
            var tagId = entry.getKey();
            var replacements = entry.getValue();
            
            try {
                for (Config.BiomeReplacement replacement : replacements)
                {
                    Holder<Biome> newBiome = getBiomeHolder(replacement.targetBiome, biomeRegistry);
                    // Unwrapping the biome key
                    TagKey<Biome> tagKey = getBiomeTagKey(tagId);
                    var biomesInTag = biomeRegistry.getTagOrEmpty(tagKey);
                    for (var oldBiome : biomesInTag)
                    {
                        BiomeReplacer.log("adding " + oldBiome.getRegisteredName()); //TODO remove
                        replacementRules.putIfAbsent(oldBiome, newBiome);
                    }
                }
                rulesTag++;
            } catch (Exception e) {
                logWarn(String.format("Ignoring tag rule \"#%s\" - %s", tagId, e.getMessage()));
                rulesIgnored++;
            }
        }
        
        log(String.format("Loaded %d rules (%d direct, %d tag-based) and ignored %d",
                rulesDirect + rulesTag, rulesDirect, rulesTag, rulesIgnored));
    }
    
    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry) throws Exception
    {
        if (id.equals(Config.REMOVE_BIOME_KEYWORD))
            return null;
        
        var resourceKey = getBiomeResourceKey(id);
        
        //? if >=1.21.2 {
        /*var holder = registry.get(resourceKey);
        if (holder.isPresent()) return holder.get();
        *///?} else {
        var holder = registry.getHolder(resourceKey);
        if (holder.isPresent()) return holder.get();
        //?}
        
        throw new Exception(String.format("Biome %s is not registered", id));
    }
    
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID: %s", id));
        //? if >=1.19.4
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.4
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id); // Assume id starts with '#' as passed by command logic
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag: #%s", id));
        //? if >=1.19.4
        return TagKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.4
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }
    
    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        return replacementRules.getOrDefault(original, original);
    }
}
