package net.werdei.biome_replacer.mixin.blueprint;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.werdei.biome_replacer.replacer.BlueprintReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "com.teamabnormals.blueprint.common.world.modification.ModdedBiomeSlicesManager", remap = false)
public abstract class ModdedBiomeSlicesManagerMixin
{
    @ModifyArgs(
            method = "onServerAboutToStart",
            at = @At(value = "INVOKE", target = "Lcom/teamabnormals/blueprint/common/world/modification/ModdedBiomeSource;<init>(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/ArrayList;IJJ)V", remap = false),
            remap = false,
            require = 0
    )
    private static void biome_replacer$storeDimensionHash(Args args, @Local(ordinal = 0) ResourceLocation location)
    {
        if (location != null)
            BlueprintReplacer.pushDimensionContext(location.toString());
    }

    @ModifyArgs(
            method = "onServerAboutToStart",
            at = @At(value = "INVOKE", target = "Lcom/teamabnormals/blueprint/common/world/modification/ModdedBiomeSource;<init>(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/ArrayList;IJJJ)V", remap = false),
            remap = false,
            require = 0
    )
    private static void biome_replacer$storeDimensionHashExtended(Args args, @Local(ordinal = 0) ResourceLocation location)
    {
        if (location != null)
            BlueprintReplacer.pushDimensionContext(location.toString());
    }
}