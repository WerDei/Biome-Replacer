package net.werdei.biome_replacer.replacer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;

import java.util.List;
import java.util.Objects;

public class TerraBlenderReplacer
{
    public static List<Pair<Climate.ParameterPoint, Holder<Biome>>> modifyPairList(
            List<Pair<Climate.ParameterPoint, Holder<Biome>>> original,
            Registry<Biome> biomeRegistry)
    {
        //? if >=1.21.2
        var placeholder = biomeRegistry.getOrThrow(Region.DEFERRED_PLACEHOLDER);
        //? if <1.21.2
        /*var placeholder = biomeRegistry.getHolderOrThrow(Region.DEFERRED_PLACEHOLDER);*/
        
        return original.stream()
                .map(pair -> pair.mapSecond(oldBiome -> {
                    var newBiome = VanillaReplacer.replaceIfNeeded(oldBiome);
                    // If biome is "removed", replace it with TB's placeholder so it can place a vanilla biome there
                    return newBiome != null ? newBiome : placeholder;
                }))
                .toList();
    }
    
    public static List<Holder<Biome>> modifyDeferredBiomeList(List<Holder<Biome>> originalList)
    {
        return originalList.stream()
                .map(VanillaReplacer::replaceIfNeeded)
                .filter(Objects::nonNull)
                .toList();
    }
}
