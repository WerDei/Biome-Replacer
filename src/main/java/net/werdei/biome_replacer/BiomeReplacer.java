package net.werdei.biome_replacer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.config.Config;
import net.werdei.biome_replacer.integration.BiolithIntegration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//? if >=1.20.2 {
/*import net.minecraft.core.registries.Registries;*/
//?} else if lexforge {
import net.minecraftforge.registries.ForgeRegistries;
//?}


public class BiomeReplacer
{
    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";
    private static volatile Map<ResourceKey<Biome>, List<BiomeReplacementEntry>> replacementRules;
    private static volatile Map<TagKey<Biome>, List<BiomeReplacementEntry>> tagReplacementRules;
    private static volatile boolean usingBiolith = false;

    // Class to store replacement information along with probability
    private static class BiomeReplacementEntry {
        public final Holder<Biome> targetBiome;
        public final double probability;

        public BiomeReplacementEntry(Holder<Biome> targetBiome, double probability) {
            this.targetBiome = targetBiome;
            this.probability = probability;
        }
    }

    public static void initialize()
    {
        Config.getOrCreateFile();
    }

    public static void prepareReplacementRules(Registry<Biome> biomeRegistry)
    {
        // Load config first, regardless of whether we use Biolith or not
        Config.reload();
        
        // First, try to use Biolith if available
        if (BiolithIntegration.initializeBiolithIntegration(biomeRegistry)) {
            usingBiolith = true;
            // Clear our own replacement rules since Biolith will handle everything
            replacementRules = new HashMap<>();
            tagReplacementRules = new HashMap<>();
            log("Biome replacement will be handled by Biolith. Direct mixin replacement disabled.");
            return;
        }

        // Fall back to our own replacement system
        usingBiolith = false;
        replacementRules = new HashMap<>();
        tagReplacementRules = new HashMap<>();

        log("Using direct biome replacement system (Biolith not available or integration failed).");

        // Process direct biome replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.rules.entrySet()) {
            String oldBiomeId = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();

            try {
                ResourceKey<Biome> oldBiome = getBiomeResourceKey(oldBiomeId);
                
                for (Config.BiomeReplacement replacement : replacements) {
                    Holder<Biome> newBiome = getBiomeHolder(replacement.targetBiome, biomeRegistry);

                    // Skip null biomes (removal rules) for direct replacement system - they're only for Biolith
                    if (newBiome == null) {
                        logWarn(String.format("Biome removal rule for \"%s\" skipped - removal only supported with Biolith integration", oldBiomeId));
                        continue;
                    }

                    // Add to replacement rules with probability
                    replacementRules.computeIfAbsent(oldBiome, k -> new ArrayList<>())
                            .add(new BiomeReplacementEntry(newBiome, replacement.probability));
                }
            } catch (Exception e) {
                logWarn(String.format("Ignoring rules for biome \"%s\" - %s", oldBiomeId, e.getMessage()));
            }
        }

        // Process tag-based replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.tagRules.entrySet()) {
            String tagId = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();

            try {
                TagKey<Biome> tagKey = getBiomeTagKey("#" + tagId);

                for (Config.BiomeReplacement replacement : replacements) {
                    Holder<Biome> newBiome = getBiomeHolder(replacement.targetBiome, biomeRegistry);

                    // Skip null biomes (removal rules) for direct replacement system - they're only for Biolith
                    if (newBiome == null) {
                        logWarn(String.format("Biome removal rule for tag \"#%s\" skipped - removal only supported with Biolith integration", tagId));
                        continue;
                    }

                    // Add to tag replacement rules with probability
                    tagReplacementRules.computeIfAbsent(tagKey, k -> new ArrayList<>())
                            .add(new BiomeReplacementEntry(newBiome, replacement.probability));
                }
            } catch (Exception e) {
                logWarn(String.format("Ignoring tag rule \"#%s\" - %s", tagId, e.getMessage()));
            }
        }

        int directRuleCount = countDirectRules();
        int tagRuleCount = countTagRules();
        int totalRules = directRuleCount + tagRuleCount;
        int configRuleCount = countConfigRules();

        log(String.format("Loaded %d rules (%d direct, %d tag-based) and ignored %d rules",
                totalRules, directRuleCount, tagRuleCount, configRuleCount - totalRules));
    }

    private static int countDirectRules() {
        return replacementRules.values().stream().mapToInt(List::size).sum();
    }

    private static int countTagRules() {
        return tagReplacementRules.values().stream().mapToInt(List::size).sum();
    }

    private static int countConfigRules() {
        return Config.rules.values().stream().mapToInt(List::size).sum() +
               Config.tagRules.values().stream().mapToInt(List::size).sum();
    }

    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
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

    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.substring(1)); // Assume id starts with '#' as passed by command logic
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

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        // If Biolith is handling replacements, don't do anything in our mixin
        if (usingBiolith) {
            return original;
        }

        if (noReplacements()) return original;

        // Check for specific biome replacement
        ResourceKey<Biome> key = original.unwrapKey().orElse(null);
        if (key != null) {
            List<BiomeReplacementEntry> candidates = replacementRules.get(key);
            if (candidates != null) {
                return selectReplacementFromCandidates(candidates, original);
            }
        }

        // Check for tag-based replacement
        for (Map.Entry<TagKey<Biome>, List<BiomeReplacementEntry>> entry : tagReplacementRules.entrySet()) {
            if (original.is(entry.getKey())) {
                return selectReplacementFromCandidates(entry.getValue(), original);
            }
        }

        return original;
    }

    /**
     * Correctly handles probability distribution for multiple replacement candidates.
     * Each candidate gets its specified probability independently.
     * Uses deterministic randomness based on biome identity and rule configuration for consistent world generation.
     */
    private static Holder<Biome> selectReplacementFromCandidates(List<BiomeReplacementEntry> candidates, Holder<Biome> original) {
        // Create deterministic randomness based on the biome's identity and config content hash
        // This ensures different configs produce different results while maintaining determinism within a config
        long biomeHash = original.unwrapKey().map(key -> key.location().toString().hashCode()).orElse(0);
        long configHash = getConfigContentHash();
        Random biomeRandom = new Random(biomeHash ^ configHash);
        
        // Fast path for single candidate (most common case)
        if (candidates.size() == 1) {
            BiomeReplacementEntry entry = candidates.get(0);
            // Defensive null check - should never happen due to filtering above, but be safe
            if (entry.targetBiome == null) {
                logWarn("Encountered null target biome in replacement entry - this should not happen");
                return original;
            }
            return biomeRandom.nextDouble() <= entry.probability ? entry.targetBiome : original;
        }
        
        // For multiple candidates, collect winners efficiently
        List<BiomeReplacementEntry> winners = null; // Lazy initialization
        
        for (BiomeReplacementEntry entry : candidates) {
            // Defensive null check - should never happen due to filtering above, but be safe
            if (entry.targetBiome == null) {
                logWarn("Encountered null target biome in replacement entry - this should not happen");
                continue;
            }
            
            if (biomeRandom.nextDouble() <= entry.probability) {
                if (winners == null) {
                    winners = new ArrayList<>(candidates.size()); // Size hint for better performance
                }
                winners.add(entry);
            }
        }
        
        // If no winners, return original
        if (winners == null || winners.isEmpty()) {
            return original;
        }
        
        // If single winner, return directly
        if (winners.size() == 1) {
            return winners.get(0).targetBiome;
        }
        
        // Multiple winners: randomly select one
        return winners.get(biomeRandom.nextInt(winners.size())).targetBiome;
    }

    /**
     * Generate a stable hash based on the actual configuration content
     * to ensure consistent randomness across different runs
     */
    private static long getConfigContentHash() {
        if (replacementRules == null || tagReplacementRules == null) {
            return 0;
        }
        
        long hash = 1;
        // Hash direct rules
        for (Map.Entry<ResourceKey<Biome>, List<BiomeReplacementEntry>> entry : replacementRules.entrySet()) {
            hash = hash * 31 + entry.getKey().location().toString().hashCode();
            for (BiomeReplacementEntry replacement : entry.getValue()) {
                hash = hash * 31 + replacement.targetBiome.unwrapKey()
                    .map(key -> key.location().toString().hashCode()).orElse(0);
                hash = hash * 31 + Double.hashCode(replacement.probability);
            }
        }
        
        // Hash tag rules
        for (Map.Entry<TagKey<Biome>, List<BiomeReplacementEntry>> entry : tagReplacementRules.entrySet()) {
            hash = hash * 31 + entry.getKey().location().toString().hashCode();
            for (BiomeReplacementEntry replacement : entry.getValue()) {
                hash = hash * 31 + replacement.targetBiome.unwrapKey()
                    .map(key -> key.location().toString().hashCode()).orElse(0);
                hash = hash * 31 + Double.hashCode(replacement.probability);
            }
        }
        
        return hash;
    }

    public static boolean noReplacements()
    {
        // If using Biolith, we consider our system to have "no replacements" 
        // since Biolith handles them
        if (usingBiolith) {
            return true;
        }
        return replacementRules.isEmpty() && tagReplacementRules.isEmpty();
    }

    public static boolean isUsingBiolith()
    {
        return usingBiolith;
    }

    public static void log(String message)
    {
        LOGGER.info(LOG_PREFIX + "{}", message);
    }

    public static void logWarn(String message)
    {
        LOGGER.warn(LOG_PREFIX + "{}", message);
    }
}