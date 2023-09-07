package net.werdei.biome_replacer.mixin;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiNoiseBiomeSource.class, priority = 2000)
public abstract class MultiNoiseBiomeSourceMixin
{
    @Unique
    private boolean hasInitialised;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void parameters(Either either, CallbackInfo ci)
    {
        //TODO Why is it called 11 times??
        BiomeReplacer.addOnServerStartCallback(this::findAndReplace);
    }

    @Unique
    private void findAndReplace()
    {
        if (hasInitialised)
        {
            BiomeReplacer.logWarn("Already initialised!");
            return;
        }
        if (BiomeReplacer.noReplacements())
        {
            BiomeReplacer.log("No rules found, not replacing anything");
            return;
        }

        var biomeSource = (BiomeSourceAccessor) this;

        var possibleBiomes = biomeSource.getPossibleBiomes().get();
        var newPossibleBiomes = possibleBiomes.stream()
                .map(BiomeReplacer::replaceIfNeeded)
                .collect(ImmutableSet.toImmutableSet());

        biomeSource.setPossibleBiomes(() -> newPossibleBiomes);
        hasInitialised = true;
        BiomeReplacer.log("Biomes replaced successfully");
    }

    // TODO Does not inject into the TerraBlender's return value :c
    @ModifyReturnValue(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("RETURN"))
    private Holder<Biome> replaceBiome(Holder<Biome> original)
    {
        return BiomeReplacer.replaceIfNeeded(original);
    }
}
