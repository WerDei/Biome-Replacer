package net.werdei.biome_replacer.mixin;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.storage.WorldData;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldStem.class)
public abstract class WorldStemMixin
{
    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess layeredRegistryAccess, WorldData worldData, CallbackInfo ci)
    {
        BiomeReplacer.prepareReplacementRules(registries());
        if (BiomeReplacer.noReplacements()) return;

        var access = registries().compositeAccess();
        var levelRegistry = access.registry(Registries.LEVEL_STEM).orElseThrow();
        for (LevelStem level : levelRegistry)
        {
            ResourceKey<LevelStem> key = levelRegistry.getResourceKey(level).orElseThrow();
            if (level.generator() instanceof NoiseBasedChunkGenerator generator
                    && generator.getBiomeSource() instanceof MultiNoiseBiomeSource biomeSource)
            {
                ((BiomeReplacer.Applicator) biomeSource).biomeReplacer$applyReplacements();
                BiomeReplacer.log("Successfully replaced biomes in " + key.location());
            }
            else BiomeReplacer.log("Skipping " + key.location());
        }
    }
}
