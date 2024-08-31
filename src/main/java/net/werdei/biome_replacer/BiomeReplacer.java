package net.werdei.biome_replacer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BiomeReplacer implements ModInitializer
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LOG_PREFIX = "[BiomeReplacer] ";

    private static Map<Holder<Biome>, Holder<Biome>> rules;

    @Override
    public void onInitialize()
    {
        Config.createIfAbsent();
    }

    public static void prepareReplacementRules(LayeredRegistryAccess<RegistryLayer> registryAccess)
    {
        rules = new HashMap<>();
        var registry = registryAccess.compositeAccess().registryOrThrow(Registries.BIOME);

        Config.reload();
        for (var rule : Config.rules.entrySet())
        {
            var oldBiome = getBiomeHolder(rule.getKey(), registry);
            var newBiome = getBiomeHolder(rule.getValue(), registry);
            if (oldBiome != null && newBiome != null)
                rules.put(oldBiome, newBiome);
        }

        log("Loaded " + rules.size() + " biome replacement rules");
    }

    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry)
    {
        var resourceKey = registry.getResourceKey(registry.get(ResourceLocation.parse(id)));
        if (resourceKey.isPresent())
            return registry.getHolderOrThrow(resourceKey.get());

        logWarn("Biome " + id + " not found. The rule will be ignored.");
        return null;
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        var replacement = rules.get(original);
        return replacement == null ? original : replacement;
    }

    public static boolean noReplacements()
    {
        return rules == null || rules.isEmpty();
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