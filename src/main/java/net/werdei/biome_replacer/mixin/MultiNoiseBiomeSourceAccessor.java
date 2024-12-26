package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiNoiseBiomeSource.class)
public interface MultiNoiseBiomeSourceAccessor
{
    @Accessor @Final @Mutable
    Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> getParameters();
    
    @Accessor @Final @Mutable
    void setParameters(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> value);
}
