package net.werdei.biome_replacer.replacer;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public interface MultiNoiseBiomeSourceExtension
{
    Climate.ParameterList<Holder<Biome>> biome_replacer$getParameters();

    void biome_replacer$setParameters(Climate.ParameterList<Holder<Biome>> parameters);
}
