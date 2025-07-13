package net.werdei.biome_replacer.integration;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.config.Config;

import java.util.List;
import java.util.Map;

//? if >=1.20.2 {
import net.minecraft.core.registries.Registries;
//?} else if lexforge {
/*import net.minecraftforge.registries.ForgeRegistries;
*///?}

public class BiolithIntegration
{
    /**
     * Convert our config rules to Biolith API calls
     * This uses Biolith's programmatic API to register biome replacements
     */
    public static void initialize(Registry<Biome> biomeRegistry) {
        if (Config.rules.isEmpty() && Config.tagRules.isEmpty()) {
            BiomeReplacer.log("No replacement rules to convert for Biolith.");
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
                BiomeReplacer.logWarn(String.format("Failed to convert rules for biome \"%s\" for Biolith: %s", 
                    sourceBiome, e.getMessage()));
            }
        }
        
        // Convert tag-based replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.tagRules.entrySet()) {
            String tagId = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();
            
            try {
                convertedRules += convertTagReplacement(tagId, replacements, biomeRegistry);
            } catch (Exception e) {
                BiomeReplacer.logWarn(String.format("Failed to convert tag rule \"#%s\" for Biolith: %s", 
                    tagId, e.getMessage()));
            }
        }
        
        BiomeReplacer.log(String.format("Successfully converted %d replacement rules for Biolith integration.", convertedRules));
    }
    
    /**
     * Convert a single biome replacement rule to Biolith API calls
     */
    private static boolean convertBiomeReplacement(String sourceBiome, Config.BiomeReplacement replacement, Registry<Biome> biomeRegistry) {
        try {
            // Validate source biome exists
            ResourceKey<Biome> sourceKey = getBiomeResourceKey(sourceBiome);
            String dimension = getDimensionForBiome(sourceKey, biomeRegistry);
            
            if (replacement.targetBiome.equals(Config.REMOVE_BIOME_KEYWORD)) {
                removeBiome(dimension, sourceKey);
                BiomeReplacer.log(String.format("Registered biome removal with Biolith API: %s", sourceBiome));
            } else {
                // For replacement, validate target biome and register with Biolith
                ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                replaceBiome(dimension, sourceKey, targetKey, replacement.probability);
                BiomeReplacer.log(String.format("Registered biome replacement with Biolith API: %s -> %s (%.1f%% chance)", 
                    sourceBiome, replacement.targetBiome, replacement.probability * 100));
            }
            return true;
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to process biome replacement: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert tag-based replacement rules to Biolith API calls
     */
    private static int convertTagReplacement(String tagId, List<Config.BiomeReplacement> replacements, Registry<Biome> biomeRegistry) {
        int converted = 0;
        try {
            TagKey<Biome> tagKey = getBiomeTagKey("#" + tagId);
            
            // Iterate through all biomes in the tag
            for (Holder<Biome> biomeHolder : biomeRegistry.getTagOrEmpty(tagKey)) {
                ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().orElse(null);
                if (biomeKey == null) continue;
                
                String dimension = getDimensionForBiome(biomeKey, biomeRegistry);
                
                for (Config.BiomeReplacement replacement : replacements) {
                    if (replacement.targetBiome.equals(Config.REMOVE_BIOME_KEYWORD)) {
                        removeBiome(dimension, biomeKey);
                        BiomeReplacer.log(String.format("Registered tag-based biome removal with Biolith API: %s (from tag #%s)",
                            biomeKey.location(), tagId));
                    } else {
                        ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                        replaceBiome(dimension, biomeKey, targetKey, replacement.probability);
                        BiomeReplacer.log(String.format("Registered tag-based biome replacement with Biolith API: %s -> %s (%.1f%% chance, from tag #%s)",
                            biomeKey.location(), replacement.targetBiome, replacement.probability * 100, tagId));
                    }
                    converted++;
                }
            }
            return converted;
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to process tag replacement: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Determine dimension type for a biome (Overworld, Nether, End, or null)
     */
    private static String getDimensionForBiome(ResourceKey<Biome> biomeKey, Registry<Biome> biomeRegistry) {
        //? if >=1.21.2 {
        Holder<Biome> biomeHolder = biomeRegistry.get(biomeKey).orElse(null);
        if (biomeHolder == null) return "overworld";
        //?} else {
        /*Holder<Biome> biomeHolder = biomeRegistry.getHolder(biomeKey).orElse(null);
        if (biomeHolder == null) return "overworld";
        *///?}
        
        //? if >=1.21.2 {
        if (biomeHolder.is(TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_nether")))) return "nether";
        if (biomeHolder.is(TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_end")))) return "end";
        //?} else if >=1.20.2 {
        /*if (biomeHolder.is(TagKey.create(Registries.BIOME, new ResourceLocation("minecraft", "is_nether")))) return "nether";
        if (biomeHolder.is(TagKey.create(Registries.BIOME, new ResourceLocation("minecraft", "is_end")))) return "end";
        *///?} else if lexforge {
        /*if (biomeHolder.is(TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation("is_nether")))) return "nether";
        if (biomeHolder.is(TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation("is_end")))) return "end";
        *///?} else {
        /*if (biomeHolder.is(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation("is_nether")))) return "nether";
        if (biomeHolder.is(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation("is_end")))) return "end";
        *///?}
        return "overworld"; // Default to overworld
    }
    
    /**
     * Helper to remove biome based on dimension
     */
    private static void removeBiome(String dimension, ResourceKey<Biome> sourceKey) {
        switch (dimension) {
            case "nether" -> BiomePlacement.removeNether(sourceKey);
            case "end" -> BiomePlacement.removeEnd(sourceKey);
            default -> BiomePlacement.removeOverworld(sourceKey);
        }
    }
    
    /**
     * Helper to replace biome based on dimension
     */
    private static void replaceBiome(String dimension, ResourceKey<Biome> sourceKey, ResourceKey<Biome> targetKey, double probability) {
        switch (dimension) {
            case "nether" -> BiomePlacement.replaceNether(sourceKey, targetKey, probability);
            case "end" -> BiomePlacement.replaceEnd(sourceKey, targetKey, probability);
            default -> BiomePlacement.replaceOverworld(sourceKey, targetKey, probability);
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
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);
        *///?}
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
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);
        *///?}
    }
} 