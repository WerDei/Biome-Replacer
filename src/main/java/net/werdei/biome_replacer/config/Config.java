package net.werdei.biome_replacer.config;

import net.werdei.biome_replacer.Platform;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Config
{
    public static final String FILE_NAME = "biome_replacer.properties";
    public static final String REMOVE_BIOME_KEYWORD = "null";

    // Biome replacement rules
    public static final Map<String, BiomeReplacement> rules = new HashMap<>(); // Direct biome replacements
    public static final Map<String, List<BiomeReplacement>> tagRules = new HashMap<>(); // Tag-based biome replacements

    // Class to store replacement information along with probability
    public static class BiomeReplacement {
        public final String targetBiome;
        public final double probability; // 0.0 to 1.0

        public BiomeReplacement(String targetBiome, double probability) {
            this.targetBiome = targetBiome;
            this.probability = Math.max(0.0, Math.min(1.0, probability)); // Clamp between 0 and 1
        }

        public BiomeReplacement(String targetBiome) {
            this(targetBiome, 1.0); // Default to 100% probability
        }
    }


    //Loads and processes the config file, Clears existing rules and reloads them from the file.

    public static void reload()
    {
        File file = getOrCreateFile();

        try (Scanner reader = new Scanner(file))
        {
            rules.clear();
            tagRules.clear();

            while (reader.hasNextLine())
            {
                String line = reader.nextLine().trim();

                // Skip comments and empty lines
                // Leaving "# " here for backwards compatibility
                if (line.isEmpty() || line.startsWith("!") || line.startsWith("# "))
                    continue;

                // Skip configuration options with "="
                if (line.contains("=")) {
                    continue;
                }

                // Handle biome replacement rules (old_biome > new_biome [probability])
                String[] result = line.split(">", 2);
                if (result.length == 2)
                {
                    String oldBiome = result[0].trim();
                    String newBiomeWithProb = result[1].trim();

                    // Split target biome and probability (if provided)
                    String[] biomeAndProb = newBiomeWithProb.split("\\s+", 2);
                    String newBiome = biomeAndProb[0].trim();
                    double probability = 1.0; // Default to 100% replacement

                    // Parse probability if provided
                    if (biomeAndProb.length > 1) {
                        try {
                            probability = Double.parseDouble(biomeAndProb[1].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid probability format for rule: " + line);
                        }
                    }

                    // Create the biome replacement object
                    BiomeReplacement replacement = new BiomeReplacement(newBiome, probability);

                    // Handle tag-based rules (e.g., #minecraft:is_forest)
                    if (oldBiome.startsWith("#")) {
                        String tagName = oldBiome.substring(1); // Remove '#' prefix
                        tagRules.computeIfAbsent(tagName, k -> new ArrayList<>()).add(replacement);
                    } else {
                        // Add direct biome replacement rule
                        rules.put(oldBiome, replacement);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read Biome Replacer config file: " + e.getMessage(), e);
        }
    }

     //Gets the config file or creates a new one if it doesn't exist.
    public static File getOrCreateFile()
    {
        var file = Platform.getConfigFile();

        if (file.exists()) return file;

        // Create a new config file
        try (PrintWriter writer = new PrintWriter(file))
        {
            writer.println("! Put your rules here in the format:");
            writer.println("! old_biome > new_biome [probability] (Defaults to 100%)");
            writer.println("! ");
            writer.println("! Examples (remove ! in front of one to activate it):");
            writer.println("! minecraft:dark_forest > minecraft:cherry_grove");
            writer.println("! minecraft:taiga > minecraft:desert 0.1");
            writer.println("! terralith:lavender_forest > aurorasdeco:lavender_plains");
            writer.println("! terralith:lavender_valley > aurorasdeco:lavender_plains");
            writer.println("! terralith:cave/infested_caves > minecraft:dripstone_caves");
            writer.println("! ");
            writer.println("! To remove a biome, use 'null' as the target biome:");
            writer.println("! minecraft:desert > null");
            writer.println("! ");
            writer.println("! For mass biome replacement, you can use biome tags:");
            writer.println("! #minecraft:is_forest > minecraft:desert 0.5");
            writer.println("! #minecraft:is_mountain > minecraft:badlands 0.35");
            writer.println("! ");
            writer.println("! For reference: 0.9 = 90%, 0.5 = 50%, 0.1 = 10%");
            writer.println("! For full biome list, see https://minecraft.gamepedia.com/Biome/ID");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create Biome Replacer config file: " + e.getMessage(), e);
        }
        return file;
    }

    /**
     * Gets the target biome for replacement, considering probability.
     * Returns null if the biome should be removed or the probability check fails.
     */
    public static String getTargetBiome(String sourceBiome) {
        // Check direct biome replacements
        if (rules.containsKey(sourceBiome)) {
            BiomeReplacement replacement = rules.get(sourceBiome);

            // Check if this replacement should occur based on probability
            if (Math.random() <= replacement.probability) {
                return REMOVE_BIOME_KEYWORD.equals(replacement.targetBiome) ? null : replacement.targetBiome;
            }
            return sourceBiome; // Keep original if probability check fails
        }

        return sourceBiome; // No replacement rule found
    }

    /**
     * Checks if a biome belongs to a specific tag and returns a replacement if applicable.
     * Returns null if no tag match is found or if probability check fails.
     */
    public static String getTagBasedReplacement(String sourceBiome, List<String> biomeTags) {
        if (biomeTags == null || biomeTags.isEmpty()) {
            return null;
        }

        // Check each tag the biome belongs to
        for (String tag : biomeTags) {
            List<BiomeReplacement> replacements = tagRules.get(tag);

            if (replacements != null && !replacements.isEmpty()) {
                // Use the first matching tag rule that passes the probability check
                for (BiomeReplacement replacement : replacements) {
                    if (Math.random() <= replacement.probability) {
                        return REMOVE_BIOME_KEYWORD.equals(replacement.targetBiome) ? null : replacement.targetBiome;
                    }
                }
            }
        }

        return null; // No tag match or all probability checks failed
    }
}