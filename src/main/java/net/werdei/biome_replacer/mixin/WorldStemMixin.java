package net.werdei.biome_replacer.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.19.4 {
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
//?} else {
/*import net.minecraft.core.registries.Registries;
*///?}

@Mixin(WorldStem.class)
public abstract class WorldStemMixin
{
    //? if >=1.19.4 {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData, CallbackInfo ci)
    {
        var registryAccess = layeredRegistryAccess.compositeAccess();
        //? if >=1.21.2
        BiomeReplacer.doReplacement(registryAccess.lookupOrThrow(Registries.BIOME), registryAccess.lookupOrThrow(Registries.LEVEL_STEM));
        //? if <1.21.2
        /*BiomeReplacer.doReplacement(registryAccess.registryOrThrow(Registries.BIOME), registryAccess.registryOrThrow(Registries.LEVEL_STEM));*/
    }

    //?} else {
    /*@Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, RegistryAccess.Frozen frozen, WorldData worldData, CallbackInfo ci)
    {
        var dimensionRegistry = worldData.worldGenSettings().dimensions();
        var biomeRegistry = frozen.registryOrThrow(Registry.BIOME_REGISTRY);
        BiomeReplacer.doReplacement(biomeRegistry, dimensionRegistry);
    }
    *///?}
}