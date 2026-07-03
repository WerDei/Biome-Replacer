package net.werdei.biome_replacer.mixin.bclib;

//? if bclib {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.werdei.biome_replacer.replacer.BCLibReplacer;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
//?}
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;

// Covers the BCLib versions that don't override possibleBiomes; the ones that do
// are handled by BCLBiomeSourceMixin
@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin
{
    //? if bclib {
    @ModifyReturnValue(method = "possibleBiomes", at = @At("RETURN"))
    private Set<Holder<Biome>> biome_replacer$includeReplacementTargets(Set<Holder<Biome>> original)
    {
        return BCLibReplacer.adjustPossibleBiomes(original, this);
    }
    //?}
}
