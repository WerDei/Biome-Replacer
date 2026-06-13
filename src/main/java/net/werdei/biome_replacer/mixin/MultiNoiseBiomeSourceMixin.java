package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.werdei.biome_replacer.replacer.MultiNoiseBiomeSourceExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.19.4
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin implements MultiNoiseBiomeSourceExtension
{
    //? if >=1.19.4 {
    // The parameter field may reference a datapack preset, so it's left untouched
    // (serialization and reloads see the original data), and replaced parameters
    // are served from a runtime field instead.

    @Shadow @Final
    private Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

    @Unique
    private Climate.ParameterList<Holder<Biome>> biome_replacer$runtimeParameters;

    @Inject(method = "parameters", at = @At("HEAD"), cancellable = true)
    private void biome_replacer$useRuntimeParameters(CallbackInfoReturnable<Climate.ParameterList<Holder<Biome>>> cir)
    {
        if (biome_replacer$runtimeParameters != null)
            cir.setReturnValue(biome_replacer$runtimeParameters);
    }

    @Override
    public Climate.ParameterList<Holder<Biome>> biome_replacer$getParameters()
    {
        return parameters.map((list) -> list, (holder) -> holder.value().parameters());
    }

    @Override
    public void biome_replacer$setParameters(Climate.ParameterList<Holder<Biome>> newParameters)
    {
        biome_replacer$runtimeParameters = newParameters;
    }

    //?} else {
    /*// Before 1.19.4 there is no parameters() unwrap method to intercept, and the
    // possible-biome set is collected eagerly, so the field is mutated directly.
    // It's a plain list there (presets are serialized through a separate field).

    @Shadow @Final @Mutable
    private Climate.ParameterList<Holder<Biome>> parameters;

    @Override
    public Climate.ParameterList<Holder<Biome>> biome_replacer$getParameters()
    {
        return parameters;
    }

    @Override
    public void biome_replacer$setParameters(Climate.ParameterList<Holder<Biome>> newParameters)
    {
        parameters = newParameters;
    }

    *///?}
}
