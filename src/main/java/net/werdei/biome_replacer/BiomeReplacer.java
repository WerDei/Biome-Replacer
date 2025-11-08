package net.werdei.biome_replacer;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.werdei.biome_replacer.config.Config;
import net.werdei.biome_replacer.replacer.VanillaReplacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class BiomeReplacer
{
    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";
    
    private final static List<Warning> unshownWarnings = new ArrayList<>();
    
    
    public static void initialize()
    {
        Config.getOrCreateFile();
    }
    
    public static void doReplacement(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        Config.reload();
        VanillaReplacer.doReplacement(biomeRegistry, stemRegistry);
    }
    
    public static void debug(String message)
    {
        LOGGER.debug(LOG_PREFIX + "{}", message);
    }

    public static void log(String message)
    {
        LOGGER.info(LOG_PREFIX + "{}", message);
    }

    public static void logWarn(String message)
    {
        LOGGER.warn(LOG_PREFIX + "{}", message);
    }
    
    
    public static void logRuleWarning(int line, String message)
    {
        unshownWarnings.add(new Warning(line, message));
        LOGGER.warn("{}Config issue on line {}: {}", LOG_PREFIX, line, message);
    }
    
    public static void showWarnings(ServerPlayer player)
    {
        if (unshownWarnings.isEmpty()) return;
        try
        {
            unshownWarnings.sort(Comparator.comparingInt(w -> w.line));
            player.sendSystemMessage(Component.literal("§6[BiomeReplacer] There are issues in the configuration file:"));
            for (var warning : unshownWarnings)
            {
                player.sendSystemMessage(Component.literal(String.format(
                        "§6Line %s:§r %s", warning.line, warning.message)));
            }
        }
        catch (NoSuchMethodError e)
        {
            // ServerPlayer.sendSystemMessage() and Component.literal() don't exist before 1.19
            logWarn("Failed to show config warnings in chat, you're probably running 1.18.2 where it's not supported.");
        }
        unshownWarnings.clear();
    }
    
    
    public record Warning(int line, String message) {}
}