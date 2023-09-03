package net.werdei.biome_replacer.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config
{
    public static final String FILE_NAME = "biome_replacer.properties";


    public static Map<String, String> rules = new HashMap<>();


    public static void createIfAbsent()
    {
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);
        if (file.exists()) return;

        try
        {
            var writer = new PrintWriter(file);
            writer.println("# Put your rules here in the format:");
            writer.println("# old_biome > new_biome");
            writer.println("# ");
            writer.println("# Examples (remove # in front of one to activate it):");
            writer.println("# minecraft:dark_forest > minecraft:cherry_grove");
            writer.println("# terralith:lavender_forest > aurorasdeco:lavender_plains");
            writer.println("# terralith:lavender_valley > aurorasdeco:lavender_plains");
            writer.println("# terralith:cave/infested_caves > minecraft:dripstone_caves");
            writer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void reload()
    {
        createIfAbsent();
        File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);

        try
        {
            var reader = new Scanner(file);
            rules.clear();
            while (reader.hasNextLine())
            {
                var line = reader.nextLine();
                if (line.length() == 0 || line.startsWith("#")) continue;
                var result = Arrays.stream(line.split(">")).map(s -> s.replace(" ", "")).toArray(String[]::new);
                rules.put(result[0], result[1]);
            }
            reader.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
