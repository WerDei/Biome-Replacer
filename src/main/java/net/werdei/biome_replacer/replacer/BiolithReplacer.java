package net.werdei.biome_replacer.replacer;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.config.Config;
import java.util.List;
import java.util.Map;
import static net.werdei.biome_replacer.BiomeReplacer.*;

//? if >=1.20.2 {
import net.minecraft.core.registries.Registries;

//?} else if lexforge {
/*import net.minecraftforge.registries.ForgeRegistries;
*///?}

public class BiolithReplacer
{
    
    /**
     * Convert our config rules to Biolith API calls
     */
    public static void registerRules(Registry<Biome> biomeRegistry)
    {
        if (Config.hasNoRules()) {
            log("No replacement rules to convert for Biolith.");
            return;
        }
        
        int convertedRules = 0;
        
        // Convert direct biome replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.rules.entrySet()) {
            String sourceBiome = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();
            
            try {
                for (Config.BiomeReplacement replacement : replacements) {
                    if (convertBiomeReplacement(sourceBiome, replacement, biomeRegistry)) {
                        convertedRules++;
                    }
                }
            } catch (Exception e) {
                logWarn(String.format("Failed to convert rules for biome \"%s\" for Biolith: %s",
                    sourceBiome, e.getMessage()));
            }
        }
        
        // Convert tag-based replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.tagRules.entrySet()) {
            String tagId = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();
            
            try {
                if (convertTagReplacement(tagId, replacements, biomeRegistry)) {
                    convertedRules += replacements.size();
                }
            } catch (Exception e) {
                logWarn(String.format("Failed to convert tag rule \"#%s\" for Biolith: %s",
                    tagId, e.getMessage()));
            }
        }
        
        log(String.format("Successfully converted %d replacement rules for Biolith integration.", convertedRules));
    }
    
    /**
     * Convert a single biome replacement rule to Biolith API calls
     */
    private static boolean convertBiomeReplacement(String sourceBiome, Config.BiomeReplacement replacement, Registry<Biome> biomeRegistry) {
        try {
            // Validate source biome exists
            ResourceKey<Biome> sourceKey = getBiomeResourceKey(sourceBiome);
            
            if (replacement.targetBiome.equals(Config.REMOVE_BIOME_KEYWORD)) {
                BiomePlacement.removeOverworld(sourceKey);
                debug(String.format("Registered biome removal with Biolith API: %s", sourceBiome));
            } else {
                // For replacement, validate target biome and register with Biolith
                ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                BiomePlacement.replaceOverworld(sourceKey, targetKey, replacement.probability);
                debug(String.format("Registered biome replacement with Biolith API: %s -> %s (%.1f%% chance)",
                    sourceBiome, replacement.targetBiome, replacement.probability * 100));
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process biome replacement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert tag-based replacement rules to Biolith API calls
     */
    private static boolean convertTagReplacement(String tagId, List<Config.BiomeReplacement> replacements, Registry<Biome> biomeRegistry) {
        try {
            TagKey<Biome> tagKey = getBiomeTagKey("#" + tagId);
            
            // For tag-based replacements, we need to iterate through all biomes in the tag
            // and apply the replacement to each one individually
            for (Holder<Biome> biomeHolder : biomeRegistry.getTagOrEmpty(tagKey)) {
                ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().orElse(null);
                if (biomeKey == null) continue;
                
                for (Config.BiomeReplacement replacement : replacements) {
                    if (replacement.targetBiome.equals(Config.REMOVE_BIOME_KEYWORD)) {
                        BiomePlacement.removeOverworld(biomeKey);
                        debug(String.format("Registered tag-based biome removal with Biolith API: %s (from tag #%s)",
                            biomeKey.location(), tagId));
                    } else {
                        ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                        BiomePlacement.replaceOverworld(biomeKey, targetKey, replacement.probability);
                        debug(String.format("Registered tag-based biome replacement with Biolith API: %s -> %s (%.1f%% chance, from tag #%s)",
                            biomeKey.location(), replacement.targetBiome, replacement.probability * 100, tagId));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process tag replacement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Local helper method to create biome resource keys
     */
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID: %s", id));
        //? if >=1.20.2 {
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //?} else if lexforge {
        /*return ResourceKey.create(ForgeRegistries.BIOMES.getRegistryKey(), resourceLocation);
        *///?} else {
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
        //?}
    }
    
    /**
     * Local helper method to create biome tag keys
     */
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.substring(1));
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag: %s", id));
        //? if >=1.20.2 {
        return TagKey.create(Registries.BIOME, resourceLocation);
        //?} else if lexforge {
        /*return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), resourceLocation);
        *///?} else {
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
        //?}
    }
} 