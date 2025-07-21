package net.werdei.biome_replacer.replacer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.werdei.biome_replacer.BiomeReplacer;
import terrablender.api.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TerraBlenderReplacer
{
    public static List<Pair<Climate.ParameterPoint, Holder<Biome>>> modifyPairList(
            List<Pair<Climate.ParameterPoint, Holder<Biome>>> original,
            Registry<Biome> biomeRegistry)
    {
        return original.stream()
                .map(pair -> pair.mapSecond(oldBiome -> {
                    var newBiome = BuiltInReplacer.replaceIfNeeded(oldBiome);
                    if (oldBiome != newBiome)
                        BiomeReplacer.log("Replaced " + oldBiome.getRegisteredName());
                    // If biome was "removed", replace it with TB's placeholder so it can place a vanilla biome there
                    return newBiome != null ? newBiome : biomeRegistry.getHolderOrThrow(Region.DEFERRED_PLACEHOLDER);
                }))
                .toList();
    }
    
    public static List<Holder<Biome>> modifyDeferredBiomeList(List<Holder<Biome>> originalList)
    {
        return originalList.stream()
                .map(BuiltInReplacer::replaceIfNeeded)
                .filter(Objects::nonNull)
                .toList();
    }
}
