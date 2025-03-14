package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.19.4
/*import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;*/

@Mixin(MultiNoiseBiomeSource.class)
public interface MultiNoiseBiomeSourceAccessor
{
    //? if >=1.19.4 {
    
    /*@Accessor @Final @Mutable
    Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> getParameters();
    
    @Accessor @Final @Mutable
    void setParameters(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> value);
    
    *///?} else {

    @Accessor @Final @Mutable
    Climate.ParameterList<Holder<Biome>> getParameters();

    @Accessor @Final @Mutable
    void setParameters(Climate.ParameterList<Holder<Biome>> value);
    
    //?}
}
