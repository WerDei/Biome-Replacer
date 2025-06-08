package net.werdei.biome_replacer.mixin;

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
//?}
//? if >=1.20.2 {
import net.minecraft.core.registries.Registries;
//?} else if lexforge {
/*import net.minecraftforge.registries.ForgeRegistries;
*///?}

@Mixin(WorldStem.class)
public abstract class WorldStemMixin
{
    //? if >=1.19.4 {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData, CallbackInfo ci)
    {
        try {
            var registryAccess = layeredRegistryAccess.compositeAccess();
            //? if >=1.21.2 {
            BiomeReplacer.doReplacement(registryAccess.lookupOrThrow(Registries.BIOME), registryAccess.lookupOrThrow(Registries.LEVEL_STEM));
            //?} else if >=1.20.2 {
            /*BiomeReplacer.doReplacement(registryAccess.registryOrThrow(Registries.BIOME), registryAccess.registryOrThrow(Registries.LEVEL_STEM));*/
            //?} else if lexforge {
            /*Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(ForgeRegistries.BIOMES.getRegistryKey());
            
            // Use the correct built-in registry key for level stems in 1.20.1
            try {
                //? if >=1.20.2 {
                Registry<LevelStem> levelStemRegistry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
                //?} else {
                /^// For 1.20.1, use the correct built-in registry
                Registry<LevelStem> levelStemRegistry = registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.LEVEL_STEM);
                ^///?}
                BiomeReplacer.doReplacement(biomeRegistry, levelStemRegistry);
            } catch (Exception e) {
                BiomeReplacer.logWarn("Level stem registry not available, skipping biome replacement for now: " + e.getMessage());
                BiomeReplacer.logWarn("This may be normal during early world creation phases. Biome replacement may still work through Biolith integration.");
            }
            *///?} else {
            /*BiomeReplacer.doReplacement(biomeRegistry, worldData.worldGenSettings().dimensions());*/
            //?}
        } catch (Exception e) {
            BiomeReplacer.logWarn("Failed to access registries for biome replacement: " + e.getMessage());
        }
    }

    //?} else {
    /*@Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, RegistryAccess.Frozen frozen, WorldData worldData, CallbackInfo ci)
    {
        Registry<LevelStem> dimensionRegistry = worldData.worldGenSettings().dimensions();
        Registry<Biome> biomeRegistry = frozen.registryOrThrow(Registry.BIOME_REGISTRY);
        BiomeReplacer.doReplacement(biomeRegistry, dimensionRegistry);
    }
    *///?}
}