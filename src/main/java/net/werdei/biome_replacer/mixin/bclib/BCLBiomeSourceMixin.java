package net.werdei.biome_replacer.mixin.bclib;

//? if bclib {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.replacer.BCLibReplacer;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// BCLib 2.2-3.x (MC 1.19.3-1.20.4) overrides possibleBiomes in this superclass of
// its biome sources, so the vanilla hook in BiomeSourceMixin never runs for them.
// Other versions don't override it, so require = 0
@Pseudo
@Mixin(targets = "org.betterx.bclib.api.v2.generator.BCLBiomeSource", remap = false)
public abstract class BCLBiomeSourceMixin
{
    //? if bclib {
    @ModifyReturnValue(method = {"possibleBiomes", "method_28443"}, at = @At("RETURN"), require = 0)
    private Set<Holder<Biome>> biome_replacer$includeReplacementTargets(Set<Holder<Biome>> original)
    {
        return BCLibReplacer.adjustPossibleBiomes(original, this);
    }
    //?}
}
