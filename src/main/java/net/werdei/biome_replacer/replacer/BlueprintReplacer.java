package net.werdei.biome_replacer.replacer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.core.Holder;
//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
//?}

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class BlueprintReplacer
{
    //? if >=1.19.3
    private static final ResourceKey<Biome> ORIGINAL_SOURCE_MARKER_KEY = ResourceKey.create(Registries.BIOME, Objects.requireNonNull(ResourceLocation.tryParse("blueprint:original_source_marker")));
    //? if <1.19.3
    /*private static final ResourceKey<Biome> ORIGINAL_SOURCE_MARKER_KEY = ResourceKey.create(Registry.BIOME_REGISTRY, Objects.requireNonNull(ResourceLocation.tryParse("blueprint:original_source_marker")));*/

    private static final Map<BiomeSource, String> DIMENSION_BY_SOURCE = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> DIMENSION_CONTEXT = new ThreadLocal<>();

    private BlueprintReplacer() {}

    public static void pushDimensionContext(String dimensionId)
    {
        if (dimensionId == null || dimensionId.isEmpty())
        {
            DIMENSION_CONTEXT.remove();
            return;
        }
        DIMENSION_CONTEXT.set(dimensionId);
    }

    public static void captureDimensionFor(BiomeSource source)
    {
        var dimensionId = DIMENSION_CONTEXT.get();
        DIMENSION_CONTEXT.remove();
        if (dimensionId != null && source != null)
            DIMENSION_BY_SOURCE.put(source, dimensionId);
    }

    public static Holder<Biome> adjustBiome(Holder<Biome> original, Registry<Biome> registry, BiomeSource source)
    {
        if (registry == null)
            return original;

        if (original == null)
            return originalSourceMarker(registry);

        var dimensionId = source != null ? DIMENSION_BY_SOURCE.get(source) : null;
        var replaced = VanillaReplacer.replaceIfNeeded(original, dimensionId);
        return replaced != null ? replaced : originalSourceMarker(registry);
    }

    private static Holder<Biome> originalSourceMarker(Registry<Biome> registry)
    {
        //? if >=1.21.2
    return registry.get(ORIGINAL_SOURCE_MARKER_KEY).orElseThrow(() -> new IllegalStateException("Blueprint original source marker biome is missing: " + ORIGINAL_SOURCE_MARKER_KEY));
        //? if <1.21.2
        /*return registry.getHolderOrThrow(ORIGINAL_SOURCE_MARKER_KEY);*/
    }
}