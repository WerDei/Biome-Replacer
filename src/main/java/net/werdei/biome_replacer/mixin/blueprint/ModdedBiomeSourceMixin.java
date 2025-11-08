package net.werdei.biome_replacer.mixin.blueprint;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.werdei.biome_replacer.replacer.BlueprintReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "com.teamabnormals.blueprint.common.world.modification.ModdedBiomeSource", remap = false)
public abstract class ModdedBiomeSourceMixin
{
    @Shadow(remap = false) private Registry<Biome> biomes;

    @Inject(method = "<init>(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/ArrayList;IJJ)V", at = @At("RETURN"), remap = false, require = 0)
    private void biome_replacer$captureDimensionSimple(Registry<Biome> registry, BiomeSource originalSource, ArrayList<?> slices, int size, long seed, long dimensionSeedModifier, CallbackInfo ci)
    {
        BlueprintReplacer.captureDimensionFor((BiomeSource) (Object) this);
    }

    @Inject(method = "<init>(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/ArrayList;IJJJ)V", at = @At("RETURN"), remap = false, require = 0)
    private void biome_replacer$captureDimensionExtended(Registry<Biome> registry, BiomeSource originalSource, ArrayList<?> slices, int size, long seed, long slicesSeed, long slicesZoomSeed, CallbackInfo ci)
    {
        BlueprintReplacer.captureDimensionFor((BiomeSource) (Object) this);
    }

    @ModifyExpressionValue(
            method = "getSlice",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/teamabnormals/blueprint/core/util/BiomeUtil$ModdedBiomeProvider;getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/core/Registry;)Lnet/minecraft/core/Holder;",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private Holder<Biome> biome_replacer$adjustBiome(Holder<Biome> biome)
    {
        return BlueprintReplacer.adjustBiome(biome, this.biomes, (BiomeSource) (Object) this);
    }
}