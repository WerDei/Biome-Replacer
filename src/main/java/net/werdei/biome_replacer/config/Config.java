package net.werdei.biome_replacer.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {

    public static final String FILE_NAME = "biome_replacer.properties";
    public static final Map<String, String> rules = new HashMap<>();

    public static void createIfAbsent() {
        File file = getConfigFile();
        if (file.exists()) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("# Put your rules here in the format:");
            writer.println("# old_biome > new_biome");
            writer.println("# ");
            writer.println("# Examples (remove # in front of one to activate it):");
            writer.println("# minecraft:dark_forest > minecraft:cherry_grove");
            writer.println("# terralith:lavender_forest > aurorasdeco:lavender_plains");
            writer.println("# terralith:lavender_valley > aurorasdeco:lavender_plains");
            writer.println("# terralith:cave/infested_caves > minecraft:dripstone_caves");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config file: " + e.getMessage(), e);
        }
    }

    public static void reload() {
        createIfAbsent();
        File file = getConfigFile();

        try (Scanner reader = new Scanner(file)) {
            rules.clear();
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] result = line.split(">");
                if (result.length == 2) {
                    rules.put(result[0].trim(), result[1].trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + e.getMessage(), e);
        }
    }

    private static File getConfigFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return configDir.resolve(FILE_NAME).toFile();
    }
}
