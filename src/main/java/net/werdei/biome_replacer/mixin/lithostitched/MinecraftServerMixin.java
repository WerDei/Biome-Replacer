package net.werdei.biome_replacer.mixin.lithostitched;

import net.minecraft.server.MinecraftServer;
import net.werdei.biome_replacer.replacer.LithostitchedReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lithostitched applies its biome injectors right before loadLevel is invoked,
// so this is the earliest point where the rules can be re-applied on top of them.
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void biome_replacer$afterLithostitchedInjectors(CallbackInfo ci)
    {
        var server = (MinecraftServer) (Object) this;
        LithostitchedReplacer.afterInjectorsApplied(server.registryAccess());
    }
}
