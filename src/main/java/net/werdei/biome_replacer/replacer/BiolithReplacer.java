package net.werdei.biome_replacer.replacer;

//? if biolith {
import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import java.util.HashSet;
import java.util.Set;
//?}
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.Platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BiolithReplacer
{
    private static final String BIOLITH_ID = "biolith";

    private static final Map<Object, String> dimensionByCoordinator = new ConcurrentHashMap<>();
    private static Boolean available;

    private BiolithReplacer() {}

    // Implemented on Biolith's DimensionBiomePlacement by DimensionBiomePlacementMixin,
    // proving the replacement hook actually applied to the installed Biolith version.
    public interface Hooked {}

    public static boolean isAvailable()
    {
        if (available == null)
        {
            available = Platform.isModLoaded(BIOLITH_ID) && isHookApplied();
            if (available)
                BiomeReplacer.log("Biolith detected, replacement rules for vanilla dimensions will be applied through it");
        }
        return available;
    }

    private static boolean isHookApplied()
    {
        try
        {
            if (Hooked.class.isAssignableFrom(Class.forName("com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement")))
                return true;
        }
        catch (ClassNotFoundException | LinkageError ignored) {}
        BiomeReplacer.logWarn("Biolith is present, but its internals are not the expected shape (unsupported Biolith version?). Falling back to direct replacement.");
        return false;
    }

    // Called by DimensionBiomePlacementMixin with Biolith's final biome pick for a position,
    // after Biolith applied its own replacements and sub-biomes. This is the only point where
    // biomes that exist purely as Biolith replacements or sub-biomes can be reached.
    public static Holder<Biome> adjustFinalBiome(Holder<Biome> original, Object coordinator)
    {
        // When registration failed, VanillaReplacer already applied the rules to the
        // noise parameters, and applying them here again would compound rules like
        // "a > b" and "b > c" into "a > c".
        if (available != Boolean.TRUE)
            return original;

        var dimensionId = dimensionByCoordinator.computeIfAbsent(coordinator, BiolithReplacer::resolveDimension);
        var replaced = VanillaReplacer.replaceIfNeeded(original, dimensionId.isEmpty() ? null : dimensionId);
        // Removal rules (null) only work through the noise parameter lists, handled at registration
        return replaced != null ? replaced : original;
    }

    // Matched by class name because this method must compile without Biolith on the
    // class path (pre-1.20 targets), so it can't reference the coordinator singletons.
    private static String resolveDimension(Object coordinator)
    {
        var name = coordinator.getClass().getSimpleName();
        if (name.contains("Overworld")) return "minecraft:overworld";
        if (name.contains("Nether")) return "minecraft:the_nether";
        if (name.contains("End")) return "minecraft:the_end";
        return "";
    }

    //? if biolith {
    private static final Set<ResourceKey<DimensionType>> VANILLA_DIMENSION_TYPES = Set.of(
            BuiltinDimensionTypes.OVERWORLD, BuiltinDimensionTypes.NETHER, BuiltinDimensionTypes.END);

    // Biolith requests can't be unregistered, so everything sent to it is remembered
    // to skip duplicate requests and to detect removed rules that need a game restart.
    private static final Set<String> sessionRegistrations = new HashSet<>();

    public static boolean claims(Holder<DimensionType> dimensionType)
    {
        return isAvailable() && dimensionType.unwrapKey().map(VANILLA_DIMENSION_TYPES::contains).orElse(false);
    }

    public static void doReplacement(Registry<LevelStem> stemRegistry)
    {
        if (!isAvailable()) return;

        try
        {
            registerRules(stemRegistry);
        }
        catch (LinkageError e)
        {
            // Happens when the installed Biolith is too old to have the expected API.
            // Deactivating hands the vanilla dimensions back to VanillaReplacer, which runs after us.
            available = false;
            BiomeReplacer.logWarn("Biolith integration failed, likely due to an unsupported Biolith version. Falling back to direct replacement. Error: " + e);
        }
    }

    private static void registerRules(Registry<LevelStem> stemRegistry)
    {
        var claimedTypes = new HashSet<ResourceKey<DimensionType>>();
        var desiredRemovals = new HashSet<String>();

        for (var levelStem : stemRegistry.entrySet())
        {
            var stem = levelStem.getValue();
            var typeKey = stem.type().unwrapKey().orElse(null);
            if (typeKey == null || !VANILLA_DIMENSION_TYPES.contains(typeKey))
                continue;

            var dimensionId = levelStem.getKey().identifier().toString();

            // Biolith applies replacements per dimension type, and binds each type
            // to the first dimension using it, so only that dimension can be handled.
            if (!claimedTypes.add(typeKey))
            {
                if (VanillaReplacer.hasDimensionRules(dimensionId))
                    BiomeReplacer.logGeneralWarning(String.format(
                            "With Biolith installed, dimension-specific rules for '%s' can't be applied, because it shares a dimension type with another dimension", dimensionId));
                continue;
            }

            registerDimension(typeKey, dimensionId, stem, desiredRemovals);
        }

        var stale = sessionRegistrations.stream()
                .filter(r -> r.startsWith("remove|") && !desiredRemovals.contains(r))
                .map(r -> r.substring(r.lastIndexOf('|') + 1))
                .distinct().sorted().toList();
        if (!stale.isEmpty())
            BiomeReplacer.logGeneralWarning(String.format(
                    "Removal rules were changed, but Biolith can't undo a removal: %s will stay removed until the game is restarted.",
                    String.join(", ", stale)));
    }

    private static void registerDimension(ResourceKey<DimensionType> type, String dimensionId, LevelStem stem, Set<String> desiredRemovals)
    {
        var rules = VanillaReplacer.effectiveRules(dimensionId);
        if (rules.isEmpty())
        {
            BiomeReplacer.log("No rules apply to " + dimensionId + ", nothing to register with Biolith");
            return;
        }

        if (wouldRemoveEveryBiome(stem, rules))
        {
            BiomeReplacer.logWarn("Rules would remove every biome in " + dimensionId + ", which is not possible. Leaving it untouched");
            return;
        }

        var active = 0;
        for (var rule : rules.entrySet())
        {
            var from = rule.getKey().unwrapKey().orElse(null);
            var to = rule.getValue() == null ? null : rule.getValue().unwrapKey().orElse(null);
            if (from == null || (rule.getValue() != null && to == null))
                continue;

            if (to == null)
            {
                // Biolith only applies removals to noise selection, which the End doesn't use
                if (BuiltinDimensionTypes.END.equals(type))
                {
                    BiomeReplacer.logGeneralWarning(String.format(
                            "Removing End biomes isn't supported, ignoring removal of '%s'. Replace it with another biome instead", from.identifier()));
                    continue;
                }
                //? if <1.20.5 {
                /*BiomeReplacer.logGeneralWarning(String.format(
                        "Removing biome '%s' isn't supported with Biolith on this version, ignoring rule", from.identifier()));
                continue;
                *///?} else {
                var registration = "remove|" + type.identifier() + "|" + from.identifier();
                if (sessionRegistrations.add(registration))
                    remove(type, from);
                desiredRemovals.add(registration);
                //?}
            }
            else
            {
                // Replacement happens at biome lookup (see adjustFinalBiome), but the target
                // biome is placed at an unreachable noise point so its features and structures
                // are known to the biome source, the same way Biolith injects its replacements.
                var registration = "place|" + type.identifier() + "|" + to.identifier();
                if (sessionRegistrations.add(registration))
                    placeOutOfRange(type, to);
            }
            active++;
        }

        BiomeReplacer.log(String.format("Applying %d replacement rules to %s through Biolith", active, dimensionId));
    }

    private static boolean wouldRemoveEveryBiome(LevelStem stem, Map<Holder<Biome>, Holder<Biome>> rules)
    {
        if (!(stem.generator() instanceof NoiseBasedChunkGenerator generator)
                || !(generator.getBiomeSource() instanceof MultiNoiseBiomeSource biomeSource))
            return false;

        var parameters = ((MultiNoiseBiomeSourceExtension) biomeSource).biome_replacer$getParameters();
        for (var value : parameters.values())
            if (rules.getOrDefault(value.getSecond(), value.getSecond()) != null)
                return false;
        return true;
    }

    private static void placeOutOfRange(ResourceKey<DimensionType> type, ResourceKey<Biome> biome)
    {
        var outOfRange = Climate.parameters(3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f);
        if (BuiltinDimensionTypes.OVERWORLD.equals(type))
            BiomePlacement.addOverworld(biome, outOfRange);
        else if (BuiltinDimensionTypes.NETHER.equals(type))
            BiomePlacement.addNether(biome, outOfRange);
        else
            BiomePlacement.addEnd(biome, outOfRange);
    }

    //? if >=1.20.5 {
    private static void remove(ResourceKey<DimensionType> type, ResourceKey<Biome> from)
    {
        if (BuiltinDimensionTypes.OVERWORLD.equals(type))
            BiomePlacement.removeOverworld(from);
        else if (BuiltinDimensionTypes.NETHER.equals(type))
            BiomePlacement.removeNether(from);
        else
            BiomePlacement.removeEnd(from);
    }
    //?}
    //?}
}
