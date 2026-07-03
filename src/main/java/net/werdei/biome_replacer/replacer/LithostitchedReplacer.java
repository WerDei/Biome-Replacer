package net.werdei.biome_replacer.replacer;

//? if lithostitched {
import com.mojang.datafixers.util.Pair;
import dev.worldgen.lithostitched.api.event.AddBiomeInjectorsEvent;
import dev.worldgen.lithostitched.api.worldgen.biomeinjector.BiomeInjector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
//?}
import net.minecraft.core.RegistryAccess;
import net.werdei.biome_replacer.BiomeReplacer;
import net.werdei.biome_replacer.Platform;

public final class LithostitchedReplacer
{
    private static final String LITHOSTITCHED_ID = "lithostitched";

    private static Boolean available;
    private static boolean injectorsRequested;

    private LithostitchedReplacer() {}

    public static boolean isAvailable()
    {
        //? if !lithostitched
        /*return false;*/
        //? if lithostitched {
        if (available == null)
        {
            available = Platform.isModLoaded(LITHOSTITCHED_ID) && registerListener();
            if (available)
                BiomeReplacer.log("Lithostitched detected, replacement rules will be registered with it");
        }
        return available;
        //?}
    }

    // Replacements are registered as Lithostitched injectors, so the noise parameter
    // lists are left to removal rules only. Applying replacements in both places
    // would compound rules like "a > b" and "b > c" into "a > c".
    public static boolean claimsReplacements()
    {
        return isAvailable();
    }

    // Called by MinecraftServerMixin once Lithostitched has applied its injectors.
    // Re-running the parameter edits here lets removal rules see biomes that add_points
    // injections put into the parameter lists, and falls back to direct replacement
    // if the integration didn't engage.
    public static void afterInjectorsApplied(RegistryAccess registries)
    {
        //? if lithostitched {
        if (available != Boolean.TRUE)
            return;

        if (!injectorsRequested && VanillaReplacer.hasAnyRules())
        {
            available = false;
            BiomeReplacer.logGeneralWarning("Lithostitched is present, but its internals are not the expected shape (unsupported Lithostitched version?). Falling back to direct replacement.");
        }

        VanillaReplacer.doReplacement(levelStems(registries));
        //?}
    }

    //? if lithostitched {
    // Lithostitched picks the lowest priority first in its replace_fully pass,
    // so this makes config rules win over other mods' replacements
    private static final int INJECTOR_PRIORITY = Integer.MIN_VALUE;

    private static List<Pair<Identifier, BiomeInjector>> cachedInjectors;

    public static void onWorldLoad()
    {
        cachedInjectors = null;
        injectorsRequested = false;
        isAvailable();
    }

    private static boolean registerListener()
    {
        try
        {
            AddBiomeInjectorsEvent.EVENT.register(LithostitchedReplacer::addInjectors);
            return true;
        }
        catch (LinkageError e)
        {
            // Lithostitched before 1.6 has no biome placement, so nothing is missed
            BiomeReplacer.log("The installed Lithostitched version has no biome layout API, using direct replacement");
            return false;
        }
    }

    // Invoked once per dimension; the consumer filters out other dimensions' injectors
    private static void addInjectors(RegistryAccess registries, BiConsumer<Identifier, BiomeInjector> consumer)
    {
        injectorsRequested = true;
        if (available != Boolean.TRUE)
            return;

        if (cachedInjectors == null)
            cachedInjectors = buildInjectors(registries);
        for (var injector : cachedInjectors)
            consumer.accept(injector.getFirst(), injector.getSecond());
    }

    private static List<Pair<Identifier, BiomeInjector>> buildInjectors(RegistryAccess registries)
    {
        var result = new ArrayList<Pair<Identifier, BiomeInjector>>();
        for (var levelStem : levelStems(registries).entrySet())
        {
            var stemKey = levelStem.getKey();

            //? if bclib {
            // BCLib dimensions are replaced at lookup, an injector on top
            // would apply the rules a second time
            if (BCLibReplacer.claims(levelStem.getValue()))
                continue;
            //?}

            var dimensionId = stemKey.identifier().toString();
            var levelKey = ResourceKey.create(Registries.DIMENSION, stemKey.identifier());
            var registered = 0;

            for (var rule : VanillaReplacer.effectiveRules(dimensionId).entrySet())
            {
                // Removal rules only work through the noise parameter lists
                if (rule.getValue() == null)
                    continue;

                var id = Identifier.tryParse("biome_replacer:" + dimensionId.replace(':', '_') + "/" + result.size());
                result.add(new Pair<>(id, BiomeInjector.builder(levelKey)
                        .priority(INJECTOR_PRIORITY)
                        .replaceFully(rule.getKey(), rule.getValue())));
                registered++;
            }

            if (registered > 0)
                BiomeReplacer.log(String.format("Applying %d replacement rules to %s through Lithostitched", registered, dimensionId));
        }
        return result;
    }

    private static Registry<LevelStem> levelStems(RegistryAccess registries)
    {
        //? if >=1.21.2
        return registries.lookupOrThrow(Registries.LEVEL_STEM);
        //? if <1.21.2
        /*return registries.registryOrThrow(Registries.LEVEL_STEM);*/
    }
    //?}
}
