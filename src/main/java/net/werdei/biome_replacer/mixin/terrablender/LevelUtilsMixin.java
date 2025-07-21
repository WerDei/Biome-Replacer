package net.werdei.biome_replacer.mixin.terrablender;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.replacer.TerraBlenderReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import terrablender.util.LevelUtils;

import java.util.List;

@Mixin(LevelUtils.class)
public class LevelUtilsMixin
{
    // TODO Doesn't compile on forge for whatever reason, can't be bothered to fix this for now. It's relevant
    //  for "possibleBiomes" list which is only used in /locate biome (as far as I know), so not a high priority
    // Also throws warnings on compile, but seems to be work fine anyway? mixins are fucking weird
    
    //? if !oldforge {
    @ModifyArg(
            method = "initializeBiomes",
            at = @At(
                    value = "INVOKE",
                    target = "Lterrablender/worldgen/IExtendedBiomeSource;appendDeferredBiomesList(Ljava/util/List;)V"))
    private static List<Holder<Biome>> replaceBiomes(List<Holder<Biome>> original)
    {
        return TerraBlenderReplacer.modifyDeferredBiomeList(original);
    }
    //?}
}
