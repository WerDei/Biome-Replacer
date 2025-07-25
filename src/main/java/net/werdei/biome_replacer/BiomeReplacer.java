package net.werdei.biome_replacer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.werdei.biome_replacer.config.Config;
import net.werdei.biome_replacer.replacer.VanillaReplacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class BiomeReplacer
{
    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";

    private static List<Warning> unshownWarnings = new ArrayList<>();

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
//        unshownWarnings.add(new Warning(line, message));
        LOGGER.warn("{}Config issue on line {}: {}", LOG_PREFIX, line, message);
    }
    
    public static List<Warning> getWarningsAndClear()
    {
        var out = unshownWarnings;
        unshownWarnings = new ArrayList<>();
        return out;
    }
    
    
    public record Warning(int line, String message) {}
}