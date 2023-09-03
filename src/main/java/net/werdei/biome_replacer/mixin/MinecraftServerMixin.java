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
    //TODO Find a more appropriate injection point, maybe?
    @Inject(method = "<init>", at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/server/WorldStem;worldData()Lnet/minecraft/world/level/storage/WorldData;"))
    private void onServerStart(CallbackInfo ci)
    {
        BiomeReplacer.prepareReplacementRules((MinecraftServer)(Object)this);
    }
}
