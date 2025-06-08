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
    private static Map<ResourceKey<Biome>, List<BiomeReplacementEntry>> replacementRules;
    private static Map<TagKey<Biome>, List<BiomeReplacementEntry>> tagReplacementRules;
    
    public static void doReplacement(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        PrepareRules(biomeRegistry);
        
        for (var entry : stemRegistry.entrySet())
        {
            var levelId = entry.getKey().location();
            var level = entry.getValue();
            
            if (level.generator() instanceof NoiseBasedChunkGenerator generator
                    && generator.getBiomeSource() instanceof MultiNoiseBiomeSource)
            {
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
    }
    
    private static void PrepareRules(Registry<Biome> biomeRegistry)
    {
        replacementRules = new HashMap<>();
        tagReplacementRules = new HashMap<>();
        
        // simple biome replacements
        for (Map.Entry<String, List<Config.BiomeReplacement>> entry : Config.rules.entrySet()) {
            String oldBiomeId = entry.getKey();
            List<Config.BiomeReplacement> replacements = entry.getValue();
            
            try
            {
                ResourceKey<Biome> oldBiome = getBiomeResourceKey(oldBiomeId);
                
                for (Config.BiomeReplacement replacement : replacements)
                {
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
            }
            catch (Exception e) {
                logWarn(String.format("Ignoring rules for biome \"%s\" - %s", oldBiomeId, e.getMessage()));
            }
        }
        
        // tag-based replacements
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
        
        int directRuleCount = replacementRules.values().stream().mapToInt(List::size).sum();
        int tagRuleCount = tagReplacementRules.values().stream().mapToInt(List::size).sum();
        int totalRules = directRuleCount + tagRuleCount;
        
        log(String.format("Loaded %d rules (%d direct, %d tag-based) and ignored %d rules",
                totalRules, directRuleCount, tagRuleCount, Config.getRuleCount() - totalRules));
    }
    
    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
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
    
    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.substring(1)); // Assume id starts with '#' as passed by command logic
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
    
    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry) throws Exception
    {
        if (id.equals(Config.REMOVE_BIOME_KEYWORD))
            return null;
        
        var resourceKey = getBiomeResourceKey(id);
        
        //? if >=1.21.2 {
        var holder = registry.get(resourceKey);
        if (holder.isPresent()) return holder.get();
        //?} else {
        /*var holder = registry.getHolder(resourceKey);
        if (holder.isPresent()) return holder.get();
        *///?}
        
        throw new Exception(String.format("Biome %s is not registered", id));
    }
    
    private static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        if (noRules()) return original;
        
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
    
    
    public static boolean noRules()
    {
        return replacementRules.isEmpty() && tagReplacementRules.isEmpty();
    }
    
    
    // Class to store replacement information along with probability
    private record BiomeReplacementEntry(Holder<Biome> targetBiome, double probability) {}
}
