package net.werdei.biome_replacer.replacer;

//? if bclib {
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.ArrayList;
//?}
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.Platform;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BCLibReplacer
{
    private static final String BCLIB_ID = "bclib";
    // Holds BCLib's biome sources on 1.21+, and can be installed without BCLib
    private static final String WOVER_ID = "wover";

    private static final Map<Class<?>, String> dimensionBySourceClass = new ConcurrentHashMap<>();
    private static Boolean available;

    private BCLibReplacer() {}

    // Implemented on BCLib's nether and end biome sources by BCLibBiomeSourceMixin,
    // proving the replacement hook actually applied to the installed BCLib version.
    public interface Hooked {}

    public static boolean isAvailable()
    {
        //? if !bclib
        /*return false;*/
        //? if bclib {
        if (available == null)
        {
            available = (Platform.isModLoaded(BCLIB_ID) || Platform.isModLoaded(WOVER_ID)) && isHookApplied();
            if (available)
                BiomeReplacer.log("BCLib detected, rules for the Nether and the End will be applied through it");
        }
        return available;
        //?}
    }

    // Called by BCLibBiomeSourceMixin with the final biome pick for a position.
    // BCLib swaps out the whole biome source in its dimensions, so this is the
    // only point where rules can reach them.
    public static Holder<Biome> adjustFinalBiome(Holder<Biome> original, Object biomeSource)
    {
        if (available != Boolean.TRUE)
            return original;

        var dimensionId = dimensionBySourceClass.computeIfAbsent(biomeSource.getClass(), BCLibReplacer::resolveDimension);
        var replaced = VanillaReplacer.replaceIfNeeded(original, dimensionId.isEmpty() ? null : dimensionId);
        // Removal rules (null) can't be done here, a lookup always has to return a biome
        return replaced != null ? replaced : original;
    }

    // Replacement targets are added to possibleBiomes so the game picks up their
    // features, structures and surface rules. BCLib's pickers don't read it,
    // so the targets won't start generating on their own.
    public static Set<Holder<Biome>> adjustPossibleBiomes(Set<Holder<Biome>> original, Object biomeSource)
    {
        if (available != Boolean.TRUE || !(biomeSource instanceof Hooked))
            return original;

        var dimensionId = dimensionBySourceClass.computeIfAbsent(biomeSource.getClass(), BCLibReplacer::resolveDimension);
        var rules = VanillaReplacer.effectiveRules(dimensionId.isEmpty() ? null : dimensionId);

        Set<Holder<Biome>> adjusted = null;
        for (var rule : rules.entrySet())
        {
            var target = rule.getValue();
            if (target == null || !original.contains(rule.getKey()) || original.contains(target))
                continue;
            if (adjusted == null)
                adjusted = new LinkedHashSet<>(original);
            adjusted.add(target);
        }
        return adjusted != null ? adjusted : original;
    }

    // Matched by class name because this code must compile without BCLib on the
    // class path, and the sources carry no dimension field anyway.
    private static String resolveDimension(Class<?> sourceClass)
    {
        var name = sourceClass.getSimpleName();
        if (name.contains("Nether")) return "minecraft:the_nether";
        if (name.contains("End")) return "minecraft:the_end";
        return "";
    }

    //? if bclib {
    // BCLib's biome sources in every version: 1.x on 1.18.2, 2.x-3.x on 1.19-1.20.4,
    // WorldWeaver on 1.21+
    private static final String[][] SOURCE_CLASSES = {
            {"ru.bclib.world.generator.BCLibNetherBiomeSource",
                    "ru.bclib.world.generator.BCLibEndBiomeSource"},
            {"org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource",
                    "org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource"},
            {"org.betterx.wover.generator.impl.biomesource.nether.WoverNetherBiomeSource",
                    "org.betterx.wover.generator.impl.biomesource.end.WoverEndBiomeSource"},
    };

    public static boolean claims(LevelStem stem)
    {
        return isAvailable() && stem.generator().getBiomeSource() instanceof Hooked;
    }

    private static boolean isHookApplied()
    {
        for (var sources : SOURCE_CLASSES)
        {
            try
            {
                if (Hooked.class.isAssignableFrom(Class.forName(sources[0]))
                        && Hooked.class.isAssignableFrom(Class.forName(sources[1])))
                    return true;
            }
            catch (ClassNotFoundException | LinkageError ignored) {}
        }
        BiomeReplacer.logWarn("BCLib is present, but its internals are not the expected shape (unsupported BCLib version?). Rules may not apply to the Nether and the End.");
        return false;
    }

    public static void doReplacement(Registry<LevelStem> stemRegistry)
    {
        if (!isAvailable()) return;

        for (var levelStem : stemRegistry.entrySet())
        {
            var stem = levelStem.getValue();
            if (!(stem.generator().getBiomeSource() instanceof Hooked source))
                continue;

            var dimensionId = levelStem.getKey().identifier().toString();
            var resolved = dimensionBySourceClass.computeIfAbsent(source.getClass(), BCLibReplacer::resolveDimension);
            if (!resolved.equals(dimensionId) && VanillaReplacer.hasDimensionRules(dimensionId))
                BiomeReplacer.logGeneralWarning(String.format(
                        "With BCLib installed, dimension-specific rules for '%s' can't be applied, because it uses BCLib's biome source", dimensionId));

            var replacements = 0;
            var removals = new ArrayList<String>();
            for (var rule : VanillaReplacer.effectiveRules(resolved.isEmpty() ? null : resolved).entrySet())
            {
                if (rule.getValue() != null)
                    replacements++;
                else
                    rule.getKey().unwrapKey().ifPresent(key -> removals.add(key.identifier().toString()));
            }

            if (!removals.isEmpty())
                BiomeReplacer.logGeneralWarning(String.format(
                        "Removing biomes isn't supported in %s, because it's managed by BCLib. Replace them with another biome instead: %s",
                        dimensionId, String.join(", ", removals)));
            if (replacements > 0)
                BiomeReplacer.log(String.format("Applying %d replacement rules to %s through BCLib", replacements, dimensionId));
        }
    }
    //?}
}
