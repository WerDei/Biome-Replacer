package net.werdei.biome_replacer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BiomeReplacer implements ModInitializer
{
    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";
    private static Map<ResourceKey<Biome>, Holder<Biome>> replacementRules;
    private static Map<TagKey<Biome>, Holder<Biome>> tagReplacementRules;

    @Override
    public void onInitialize()
    {
        Config.createIfAbsent();
    }

    public static void prepareReplacementRules(LayeredRegistryAccess<RegistryLayer> registryAccess)
    {
        replacementRules = new HashMap<>();
        tagReplacementRules = new HashMap<>();
        var biomeRegistry = registryAccess.compositeAccess().registryOrThrow(Registries.BIOME);

        Config.reload();
        Config.rules.forEach((oldBiomeId, newBiomeId) ->
        {
            if (oldBiomeId.startsWith("#"))
            {
                // This is a tag
                var tagKey = getBiomeTagKey(oldBiomeId);
                var newBiome = getBiomeHolder(newBiomeId, biomeRegistry);
                if (tagKey != null && newBiome != null)
                    tagReplacementRules.put(tagKey, newBiome);
            }
            else
            {
                // This is a specific biome
                var oldBiome = getBiomeResourceKey(oldBiomeId);
                var newBiome = getBiomeHolder(newBiomeId, biomeRegistry);
                if (oldBiome != null && newBiome != null)
                    replacementRules.put(oldBiome, newBiome);
            }
        });

        log(String.format("Loaded %d biome replacement rules and %d tag replacement rules", replacementRules.size(), tagReplacementRules.size()));
    }

    private static ResourceKey<Biome> getBiomeResourceKey(String id)
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
        {
            logWarn(String.format("Invalid biome ID: %s. The rule will be ignored.", id));
            return null;
        }
        return ResourceKey.create(Registries.BIOME, resourceLocation);
    }

    private static TagKey<Biome> getBiomeTagKey(String id)
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.substring(1));
        if (resourceLocation == null)
        {
            logWarn(String.format("Invalid biome tag: %s. The rule will be ignored.", id));
            return null;
        }
        return TagKey.create(Registries.BIOME, resourceLocation);
    }


    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry)
    {
        var resourceKey = getBiomeResourceKey(id);
        if (resourceKey != null)
        {
            Optional<Holder.Reference<Biome>> holder = registry.getHolder(resourceKey);
            if (holder.isPresent()) return holder.get();
        }
        logWarn(String.format("Biome %s not found. The rule will be ignored.", id));
        return null;
    }

    public static Holder<Biome> replaceIfNeeded(Holder<Biome> original)
    {
        if (noReplacements()) return original;

        // Check for specific biome replacement
        ResourceKey<Biome> key = original.unwrapKey().orElse(null);
        if (key != null && replacementRules.containsKey(key))
            return replacementRules.get(key);

        // Check for tag-based replacement
        for (Map.Entry<TagKey<Biome>, Holder<Biome>> entry : tagReplacementRules.entrySet())
            if (original.is(entry.getKey()))
                return entry.getValue();

        return original;
    }

    public static boolean noReplacements()
    {
        return replacementRules.isEmpty() && tagReplacementRules.isEmpty();
    }

    public static void log(String message)
    {
        LOGGER.info(LOG_PREFIX + "{}", message);
    }

    public static void logWarn(String message)
    {
        LOGGER.warn(LOG_PREFIX + "{}", message);
    }


    public interface Applicator
    {
        void biomeReplacer$applyReplacements();
    }
}
