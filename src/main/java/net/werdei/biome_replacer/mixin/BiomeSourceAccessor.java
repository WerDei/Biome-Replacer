package net.werdei.biome_replacer.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.function.Supplier;

@Mixin(BiomeSource.class)
public interface BiomeSourceAccessor
{
    @Accessor()
    Supplier<Set<Holder<Biome>>> getPossibleBiomes();

    @Accessor()
    void setPossibleBiomes(Supplier<Set<Holder<Biome>>> possibleBiomes);
}
