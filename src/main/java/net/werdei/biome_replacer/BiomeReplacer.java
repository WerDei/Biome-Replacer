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
    private static final Random RANDOM = new Random();
    private static Map<ResourceKey<Biome>, List<BiomeReplacementEntry>> replacementRules;
    private static Map<TagKey<Biome>, List<BiomeReplacementEntry>> tagReplacementRules;
    private static boolean usingBiolith = false;

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
        for (Map.Entry<String, Config.BiomeReplacement> entry : Config.rules.entrySet()) {
            String oldBiomeId = entry.getKey();
            Config.BiomeReplacement replacement = entry.getValue();

            try {
                ResourceKey<Biome> oldBiome = getBiomeResourceKey(oldBiomeId);
                Holder<Biome> newBiome = getBiomeHolder(replacement.targetBiome, biomeRegistry);

                // Add to replacement rules with probability
                replacementRules.computeIfAbsent(oldBiome, k -> new ArrayList<>())
                        .add(new BiomeReplacementEntry(newBiome, replacement.probability));
            } catch (Exception e) {
                logWarn(String.format("Ignoring rule \"%s > %s\" - %s", oldBiomeId, replacement.targetBiome, e.getMessage()));
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
        int count = 0;
        for (List<BiomeReplacementEntry> entries : replacementRules.values()) {
            count += entries.size();
        }
        return count;
    }

    private static int countTagRules() {
        int count = 0;
        for (List<BiomeReplacementEntry> entries : tagReplacementRules.values()) {
            count += entries.size();
        }
        return count;
    }

    private static int countConfigRules() {
        int count = Config.rules.size();
        for (List<Config.BiomeReplacement> entries : Config.tagRules.values()) {
            count += entries.size();
        }
        return count;
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
        if (key != null && replacementRules.containsKey(key)) {
            List<BiomeReplacementEntry> candidates = replacementRules.get(key);
            for (BiomeReplacementEntry entry : candidates) {
                if (RANDOM.nextDouble() <= entry.probability) {
                    return entry.targetBiome;
                }
            }
        }

        // Check for tag-based replacement
        for (Map.Entry<TagKey<Biome>, List<BiomeReplacementEntry>> entry : tagReplacementRules.entrySet()) {
            if (original.is(entry.getKey())) {
                List<BiomeReplacementEntry> candidates = entry.getValue();
                for (BiomeReplacementEntry candidate : candidates) {
                    if (RANDOM.nextDouble() <= candidate.probability) {
                        return candidate.targetBiome;
                    }
                }
            }
        }

        return original;
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