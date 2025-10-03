package net.werdei.biome_replacer.config;

import net.werdei.biome_replacer.Platform;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static net.werdei.biome_replacer.BiomeReplacer.logRuleWarning;

public class Config
{
    public static final String FILE_NAME = "biome_replacer.properties";
    public static final String REMOVE_BIOME_KEYWORD = "null";

    public static final List<Rule> rules = new ArrayList<>();


    public static void reload()
    {
        try (Scanner reader = new Scanner(getOrCreateFile()))
        {
            rules.clear();
            var lineCount = 0;
            String currentHeader = null; // null header means global (applies to all dimensions)
            while (reader.hasNextLine())
            {
                var line = reader.nextLine().trim();
                lineCount++;
                
                // Comments. Keeping "# " for backwards compatibility with old BR versions.
                if (line.isEmpty() || line.startsWith("!") || line.startsWith("# "))
                    continue;
                
                // Headers: [namespace:path]. [null] or [] are treated as global
                if (line.startsWith("[") && line.endsWith("]"))
                {
                    var headerContent = line.substring(1, line.length() - 1).trim();
                    if (headerContent.isEmpty() || headerContent.equalsIgnoreCase("null"))
                        currentHeader = null;
                    else
                        currentHeader = headerContent;
                    continue;
                }

                // Options. Might be used later.
                if (line.contains("="))
                    continue;
                
                var split = line.split(">", 2);
                if (split.length != 2)
                {
                    logRuleWarning(lineCount, "Incorrect format, should be 'old_biome > new_biome'");
                    continue;
                }
                
                var oldBiome = split[0].trim();
                var newBiomeWithProbability = split[1].trim().split("\\s+", 2);
                var newBiome = newBiomeWithProbability[0].trim();
                
                double probability = 1.0;
                if (newBiomeWithProbability.length > 1) try
                {
                    logRuleWarning(lineCount, "Chance-based replacement was removed from the mod (for now), probability will be ignored");
                    probability = Double.parseDouble(newBiomeWithProbability[1].trim());
//                    if (probability < 0.0 || probability > 1.0)
//                        logRuleIssue(lineCount, "Probability will be clamped between 0 and 1");
                }
                catch (NumberFormatException e)
                {
//                    logRuleIssue(lineCount, "Unexpected number format. If you wanted to add probability, make sure it's a valid number");
                }
                
                rules.add(new Rule(lineCount, oldBiome, newBiome, probability, currentHeader));
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
            writer.println("! Biome Replacer configuration");
            writer.println("! Format: old_biome > new_biome");
            writer.println("!");
            writer.println("! Rules before headers are applied to all worlds (dimensions)");
            writer.println("! Remove the leading '!' to activate examples below.");
            writer.println("!");
            writer.println("! Global rules (apply to every dimension):");
            writer.println("! minecraft:forest > minecraft:tundra");
            writer.println("!");
            writer.println("! Rules under a header apply only to the world (dimension) in brackets");
            writer.println("! [minecraft:overworld]");
            writer.println("! minecraft:desert > null");
            writer.println("!");
            writer.println("! [custom:dimension]");
            writer.println("! minecraft:desert > minecraft:badlands");
            writer.println("!");
            writer.println("! Special headers: [null] or [] are treated as global (all dimensions)");
            writer.println("!");
            writer.println("! Tag examples (direct replacements have priority over tags):");
            writer.println("! #minecraft:is_forest > minecraft:desert");
            writer.println("! #terralith:skylands > null");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create Biome Replacer config file: " + e.getMessage(), e);
        }
        return file;
    }
    
    
    public record Rule(int line, String from, String to, double probability, String dimension)
    {
        public Rule {
            probability = Math.max(0.0, Math.min(1.0, probability));
        }
    }
}