package net.werdei.biome_replacer.integration;

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
/*import net.minecraft.core.registries.Registries;*/
//?} else if lexforge {
import net.minecraftforge.registries.ForgeRegistries;
//?}

public class BiolithIntegration {
    private static final String BIOLITH_MOD_ID = "biolith";
    private static boolean checkedForBiolith = false;
    private static boolean biolithAvailable = false;
    
    /**
     * Check if Biolith is available at runtime
     */
    public static boolean isBiolithAvailable() {
        if (!checkedForBiolith) {
            checkedForBiolith = true;
            try {
                // Use reflection to avoid direct API dependencies that break Stonecutter
                biolithAvailable = isModLoaded(BIOLITH_MOD_ID);
            } catch (Exception e) {
                BiomeReplacer.logWarn("Failed to check for Biolith mod: " + e.getMessage());
                biolithAvailable = false;
            }
        }
        return biolithAvailable;
    }
    
    /**
     * Check if a mod is loaded using reflection to avoid loader-specific imports
     */
    private static boolean isModLoaded(String modId) {
        try {
            //? if fabric {
            /*// Use reflection to call FabricLoader.getInstance().isModLoaded(modId)
            Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoaderClass.getMethod("getInstance").invoke(null);
            return (Boolean) fabricLoaderClass.getMethod("isModLoaded", String.class).invoke(instance, modId);
            *///?} else {
            
            // For NeoForge and LexForge, try to check the mod list via reflection
            try {
                // Try NeoForge first
                Class<?> modListClass = Class.forName("net.neoforged.fml.loading.FMLLoader");
                Object modList = modListClass.getMethod("getLoadingModList").invoke(null);
                return modList.getClass().getMethod("getModFileById", String.class).invoke(modList, modId) != null;
            } catch (Exception e1) {
                // Try legacy Forge if NeoForge fails
                try {
                    Class<?> modListClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
                    Object modList = modListClass.getMethod("getLoadingModList").invoke(null);
                    return modList.getClass().getMethod("getModFileById", String.class).invoke(modList, modId) != null;
                } catch (Exception e2) {
                    return false;
                }
            }
            
            //?}
        } catch (Exception e) {
            BiomeReplacer.logWarn("Could not detect mod " + modId + " using reflection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize Biolith integration if available
     */
    public static boolean initializeBiolithIntegration(Registry<Biome> biomeRegistry) {
        if (!isBiolithAvailable()) {
            return false;
        }
        
        try {
            BiomeReplacer.log("Biolith detected! Using Biolith for biome replacement instead of direct manipulation.");
            convertConfigToBiolithAPICalls(biomeRegistry);
            return true;
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to initialize Biolith integration: " + e.getMessage());
            BiomeReplacer.logWarn("Falling back to direct biome replacement system.");
            return false;
        }
    }
    
    /**
     * Convert our config rules to Biolith API calls
     * This uses Biolith's programmatic API to register biome replacements
     */
    private static void convertConfigToBiolithAPICalls(Registry<Biome> biomeRegistry) {
        if (Config.rules.isEmpty() && Config.tagRules.isEmpty()) {
            BiomeReplacer.log("No replacement rules to convert for Biolith.");
            return;
        }
        
        int convertedRules = 0;
        
        // Convert direct biome replacements
        for (Map.Entry<String, Config.BiomeReplacement> entry : Config.rules.entrySet()) {
            String sourceBiome = entry.getKey();
            Config.BiomeReplacement replacement = entry.getValue();
            
            try {
                if (convertBiomeReplacement(sourceBiome, replacement, biomeRegistry)) {
                    convertedRules++;
                }
            } catch (Exception e) {
                BiomeReplacer.logWarn(String.format("Failed to convert rule \"%s > %s\" for Biolith: %s", 
                    sourceBiome, replacement.targetBiome, e.getMessage()));
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
                BiomeReplacer.logWarn(String.format("Failed to convert tag rule \"#%s\" for Biolith: %s", 
                    tagId, e.getMessage()));
            }
        }
        
        BiomeReplacer.log(String.format("Successfully converted %d replacement rules for Biolith integration.", convertedRules));
        BiomeReplacer.log("Note: Biolith will handle biome placement using its API system, which provides better compatibility with other biome mods.");
    }
    
    /**
     * Convert a single biome replacement rule to Biolith API calls
     */
    private static boolean convertBiomeReplacement(String sourceBiome, Config.BiomeReplacement replacement, Registry<Biome> biomeRegistry) {
        try {
            // Validate source biome exists
            ResourceKey<Biome> sourceKey = getBiomeResourceKey(sourceBiome);
            
            if (replacement.targetBiome.equals(Config.REMOVE_BIOME_KEYWORD)) {
                // For removal, use Biolith's removal API
                callBiolithRemoveOverworld(sourceKey);
                BiomeReplacer.log(String.format("Registered biome removal with Biolith API: %s", sourceBiome));
            } else {
                // For replacement, validate target biome and register with Biolith
                ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                callBiolithReplaceOverworld(sourceKey, targetKey, replacement.probability);
                BiomeReplacer.log(String.format("Registered biome replacement with Biolith API: %s -> %s (%.1f%% chance)", 
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
                        callBiolithRemoveOverworld(biomeKey);
                        BiomeReplacer.log(String.format("Registered tag-based biome removal with Biolith API: %s (from tag #%s)", 
                            biomeKey.location(), tagId));
                    } else {
                        ResourceKey<Biome> targetKey = getBiomeResourceKey(replacement.targetBiome);
                        callBiolithReplaceOverworld(biomeKey, targetKey, replacement.probability);
                        BiomeReplacer.log(String.format("Registered tag-based biome replacement with Biolith API: %s -> %s (%.1f%% chance, from tag #%s)", 
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
     * Call Biolith's API to remove a biome from Overworld generation
     * Uses reflection to avoid compile-time dependencies
     */
    private static void callBiolithRemoveOverworld(ResourceKey<Biome> biomeKey) {
        try {
            Class<?> biomePlacementClass = Class.forName("com.terraformersmc.biolith.api.biome.BiomePlacement");
            biomePlacementClass.getMethod("removeOverworld", ResourceKey.class).invoke(null, biomeKey);
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to call Biolith removeOverworld API: " + e.getMessage());
            throw new RuntimeException("Biolith API call failed", e);
        }
    }
    
    /**
     * Call Biolith's API to replace a biome in Overworld generation
     * Uses reflection to avoid compile-time dependencies
     */
    private static void callBiolithReplaceOverworld(ResourceKey<Biome> sourceKey, ResourceKey<Biome> targetKey, double probability) {
        try {
            Class<?> biomePlacementClass = Class.forName("com.terraformersmc.biolith.api.biome.BiomePlacement");
            biomePlacementClass.getMethod("replaceOverworld", ResourceKey.class, ResourceKey.class, double.class)
                .invoke(null, sourceKey, targetKey, probability);
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to call Biolith replaceOverworld API: " + e.getMessage());
            throw new RuntimeException("Biolith API call failed", e);
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
        /*return ResourceKey.create(Registries.BIOME, resourceLocation);*/
        //?} else if lexforge {
        return ResourceKey.create(ForgeRegistries.BIOMES.getRegistryKey(), resourceLocation);
        //?} else {
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
        /*return TagKey.create(Registries.BIOME, resourceLocation);*/
        //?} else if lexforge {
        return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), resourceLocation);
        //?} else {
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
        //?}
    }
} 