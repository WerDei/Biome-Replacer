package net.werdei.biome_replacer.mixin.bclib;

//? if bclib {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.injection.At;
//?}
import net.werdei.biome_replacer.replacer.BCLibReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// BCLib swaps out the whole biome source in the Nether and the End, so the final
// lookup is the only point where rules can reach every biome it places,
// sub-biomes and edges included
@Pseudo
@Mixin(targets = {
        "ru.bclib.world.generator.BCLibNetherBiomeSource",
        "ru.bclib.world.generator.BCLibEndBiomeSource",
        "org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource",
        "org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource",
        "org.betterx.wover.generator.impl.biomesource.nether.WoverNetherBiomeSource",
        "org.betterx.wover.generator.impl.biomesource.end.WoverEndBiomeSource",
}, remap = false)
public abstract class BCLibBiomeSourceMixin implements BCLibReplacer.Hooked
{
    //? if bclib {
    // getNoiseBiome by its Mojang name for dev runs and intermediary name for production
    @ModifyReturnValue(method = {"getNoiseBiome", "method_38109"}, at = @At("RETURN"), require = 1)
    private Holder<Biome> biome_replacer$applyReplacementRules(Holder<Biome> original)
    {
        return BCLibReplacer.adjustFinalBiome(original, this);
    }
    //?}
}
