package net.werdei.biome_replacer.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config
{
    public static final String FILE_NAME = "biome_replacer.properties";
    public static final String REMOVE_BIOME_KEYWORD = "null";
    public static final Map<String, String> rules = new HashMap<>();


    public static void reload()
    {
        File file = getOrCreateFile();

        try (Scanner reader = new Scanner(file))
        {
            rules.clear();
            while (reader.hasNextLine())
            {
                String line = reader.nextLine().trim();
                // Leaving "# " here for backwards compatibility
                if (line.isEmpty() || line.startsWith("!") || line.startsWith("# "))
                    continue;
                String[] result = line.split(">");
                if (result.length == 2)
                    rules.put(result[0].trim(), result[1].trim());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read Biome Replacer config file: " + e.getMessage(), e);
        }
    }

    public static File getOrCreateFile()
    {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        var file = configDir.resolve(FILE_NAME).toFile();

        if (file.exists()) return file;

        // Create a new config file
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
            writer.println("! For mass biome replacement, you can use use biome tags:");
            writer.println("! #minecraft:is_forest > minecraft:desert");
            writer.println("! #minecraft:is_mountain > minecraft:badlands");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create Biome Replacer config file: " + e.getMessage(), e);
        }
        return file;
    }
}