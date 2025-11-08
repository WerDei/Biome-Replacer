package net.werdei.biome_replacer.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin
{
    // This method is reliably called at the end of player login, on every version and loader,
    // and does not change between versions either. Yes, this is lazy, but hey - it works!
    @Inject(method = "initInventoryMenu", at = @At("TAIL"))
    private void showWarnings(CallbackInfo ci)
    {
        BiomeReplacer.showWarnings((ServerPlayer) (Object) this);
    }
}
