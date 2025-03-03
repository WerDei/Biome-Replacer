package net.werdei.biome_replacer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
//? if >=1.19.4 {
import net.minecraft.core.registries.Registries;
//?}


public class BiomeReplacer
{
    private static final Logger LOGGER = LogManager.getLogger(BiomeReplacer.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] ";
    private static Map<ResourceKey<Biome>, Holder<Biome>> replacementRules;
    private static Map<TagKey<Biome>, Holder<Biome>> tagReplacementRules;
    

    public static void initialize()
    {
        Config.getOrCreateFile();
    }

    public static void prepareReplacementRules(Registry<Biome> biomeRegistry)
    {
        replacementRules = new HashMap<>();
        tagReplacementRules = new HashMap<>();

        Config.reload();
        Config.rules.forEach((oldBiomeId, newBiomeId) ->
        {
            try
            {
                if (oldBiomeId.startsWith("#"))
                {
                    // This is a tag
                    var tagKey = getBiomeTagKey(oldBiomeId);
                    var newBiome = getBiomeHolder(newBiomeId, biomeRegistry);
                    tagReplacementRules.put(tagKey, newBiome);
                }
                else
                {
                    // This is a specific biome
                    var oldBiome = getBiomeResourceKey(oldBiomeId);
                    var newBiome = getBiomeHolder(newBiomeId, biomeRegistry);
                    replacementRules.put(oldBiome, newBiome);
                }
            }
            catch (Exception e)
            {
                logWarn(String.format("Ignoring rule \"%s > %s\" - %s", oldBiomeId, newBiomeId, e.getMessage()));
            }
        });

        var loaded = replacementRules.size() + tagReplacementRules.size();
        log(String.format("Loaded %d and ignored %d rules", loaded, Config.rules.size() - loaded));
    }

    private static ResourceKey<Biome> getBiomeResourceKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome ID: %s", id));
        //? if >=1.19.4
        return ResourceKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.4
        /*return ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }

    private static TagKey<Biome> getBiomeTagKey(String id) throws Exception
    {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.substring(1));
        if (resourceLocation == null)
            throw new Exception(String.format("Invalid biome tag: %s", id));
        //? if >=1.19.4
        return TagKey.create(Registries.BIOME, resourceLocation);
        //? if <1.19.4
        /*return TagKey.create(Registry.BIOME_REGISTRY, resourceLocation);*/
    }


    private static Holder<Biome> getBiomeHolder(String id, Registry<Biome> registry) throws Exception
    {
        if (id.equals(Config.REMOVE_BIOME_KEYWORD))
            return null;

        var resourceKey = getBiomeResourceKey(id);
        
        //? if >=1.21.2
        var holder = registry.get(resourceKey);
        //? if <1.21.2
        /*var holder = registry.getHolder(resourceKey);*/
        
        if (holder.isPresent()) return holder.get();

        throw new Exception(String.format("Biome %s is not registered", id));
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
}
