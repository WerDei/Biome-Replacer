package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin implements BiomeReplacer.Applicator
{
    @Shadow @Final @Mutable
    private Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();


    public void biomeReplacer$applyReplacements()
    {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> newParameterList = new ArrayList<>();

        for (var value : parameters().values())
        {
            var newBiome = BiomeReplacer.replaceIfNeeded(value.getSecond());
            if (newBiome == null) continue;
            newParameterList.add(new Pair<>(value.getFirst(), newBiome));
        }

        parameters = Either.left(new Climate.ParameterList<>(newParameterList));
    }
}
