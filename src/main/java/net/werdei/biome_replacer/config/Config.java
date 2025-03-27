package net.werdei.biome_replacer.config;

import net.werdei.biome_replacer.BiomeReplacer;
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

    public static final Map<String, BiomeReplacement> rules = new HashMap<>();
    public static final Map<String, List<BiomeReplacement>> tagRules = new HashMap<>();

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

                if (line.isEmpty() || line.startsWith("!") || line.startsWith("# "))
                    continue;

                if (line.contains("=")) {
                    continue;
                }

                String[] result = line.split(">", 2);
                if (result.length == 2)
                {
                    String oldBiome = result[0].trim();
                    String newBiomeWithProb = result[1].trim();

                    String[] biomeAndProb = newBiomeWithProb.split("\\s+", 2);
                    String newBiome = biomeAndProb[0].trim();
                    double probability = 1.0;

                    if (biomeAndProb.length > 1) {
                        try {
                            probability = Double.parseDouble(biomeAndProb[1].trim());
                        } catch (NumberFormatException e) {
                            BiomeReplacer.logWarn("Invalid probability format for rule, using 1.0: " + line);
                            // Keep probability = 1.0 as default
                        }
                    }

                    BiomeReplacement replacement = new BiomeReplacement(newBiome, probability);

                    if (oldBiome.startsWith("#")) {
                        String tagName = oldBiome.substring(1);
                        tagRules.computeIfAbsent(tagName, k -> new ArrayList<>()).add(replacement);
                    } else {
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

    public static File getOrCreateFile()
    {
        var file = Platform.getConfigFile();

        if (file.exists()) return file;

        try (PrintWriter writer = new PrintWriter(file))
        {
            writer.println("! Put your rules here in the format:");
            writer.println("! old_biome > new_biome");
            writer.println("! ");
            writer.println("! Examples (remove ! in front of one to activate it):");
            writer.println("! minecraft:dark_forest > minecraft:cherry_grove");
            writer.println("! terralith:lavender_forest > aurorasdeco:lavender_plains");
            writer.println("! terralith:lavender_valley > aurorasdeco:lavender_plains");
            writer.println("! terralith:cave/infested_caves > minecraft:dripstone_caves");
            writer.println("! ");
            writer.println("! For full biome list, see https://minecraft.wiki/w/Biome#Biome_IDs");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create Biome Replacer config file: " + e.getMessage(), e);
        }
        return file;
    }
}