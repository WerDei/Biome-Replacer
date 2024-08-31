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

public class BiomeReplacer implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";
    private static Map<Holder<Biome>, Holder<Biome>> replacementRules;

    @Override
    public void onInitialize() {
        Config.createIfAbsent();
    }

    public static void prepareReplacementRules(LayeredRegistryAccess<RegistryLayer> registryAccess) {
        replacementRules = new HashMap<>();
        var biomeRegistry = registryAccess.compositeAccess().registryOrThrow(Registries.BIOME);

        Config.reload();
        Config.rules.forEach((oldBiomeId, newBiomeId) -> {
            var oldBiome = getBiomeHolder(oldBiomeId, biomeRegistry);
            var newBiome = getBiomeHolder(newBiomeId, biomeRegistry);
            if (oldBiome != null && newBiome != null) {
                replacementRules.put(oldBiome, newBiome);
            }
        });

        log(String.format("Loaded %d biome replacement rules", replacementRules.size()));
    }

    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry) {
        var resourceKey = registry.getResourceKey(registry.get(ResourceLocation.tryParse(id)));
        if (resourceKey.isPresent()) {
            return registry.getHolderOrThrow(resourceKey.get());
        }

        logWarn(String.format("Biome %s not found. The rule will be ignored.", id));
        return null;
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original) {
        return replacementRules.getOrDefault(original, original);
    }

    public static boolean noReplacements() {
        return replacementRules == null || replacementRules.isEmpty();
    }

    public static void log(String message) {
        LOGGER.info(LOG_PREFIX + "{}", message);
    }

    public static void logWarn(String message) {
        LOGGER.warn(LOG_PREFIX + "{}", message);
    }
}
