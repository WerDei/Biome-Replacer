package net.werdei.biome_replacer.mixin.biolith;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.werdei.biome_replacer.replacer.BiolithReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Biolith picks the final biome here, after applying its own replacements and
// sub-biomes, so this is the only place rules can reach biomes that other mods
// register purely as Biolith replacements or sub-biomes.
@Pseudo
@Mixin(targets = "com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement", remap = false)
public abstract class DimensionBiomePlacementMixin implements BiolithReplacer.Hooked
{
    @ModifyReturnValue(method = "getReplacement", at = @At("RETURN"))
    private Holder<Biome> biome_replacer$applyReplacementRules(Holder<Biome> original)
    {
        return BiolithReplacer.adjustFinalBiome(original, this);
    }
}
