package net.werdei.biome_replacer.mixin.terra_blender;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.werdei.biome_replacer.replacer.TerraBlenderReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(value = Climate.ParameterList.class, priority = 1500)
public abstract class ClimateParameterListMixin
{
    @TargetHandler(
            mixin = "terrablender.mixin.MixinParameterList",
            name = "initializeForTerraBlender"
    )
    @ModifyArg(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/Climate$RTree;create(Ljava/util/List;)Lnet/minecraft/world/level/biome/Climate$RTree;",
                    ordinal = 1
            )
    )
    private static List<Pair<Climate.ParameterPoint, Holder<Biome>>> replaceBiomesInPairs
            (List<Pair<Climate.ParameterPoint, Holder<Biome>>> pairs, @Local Registry<Biome> biomeRegistry)
    {
        return TerraBlenderReplacer.modifyPairList(pairs, biomeRegistry);
    }
}
