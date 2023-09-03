package net.werdei.biome_replacer.mixin;

import net.minecraft.server.MinecraftServer;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onServerStart(CallbackInfo ci)
    {
        BiomeReplacer.prepareReplacementRules((MinecraftServer)(Object)this);
    }
}
