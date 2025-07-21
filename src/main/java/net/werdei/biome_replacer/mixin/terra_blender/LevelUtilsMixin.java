package net.werdei.biome_replacer.mixin.terra_blender;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.werdei.biome_replacer.replacer.TerraBlenderReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import terrablender.util.LevelUtils;

import java.util.List;

@Mixin(LevelUtils.class)
public class LevelUtilsMixin
{
    @ModifyArg(
            method = "initializeBiomes",
            at = @At(
                    value = "INVOKE",
                    target = "Lterrablender/worldgen/IExtendedBiomeSource;appendDeferredBiomesList(Ljava/util/List;)V"))
    private static List<Holder<Biome>> replaceBiomes(List<Holder<Biome>> original)
    {
        return TerraBlenderReplacer.modifyDeferredBiomeList(original);
    }
}
