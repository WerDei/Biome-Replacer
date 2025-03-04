package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.storage.WorldData;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;
//? if >=1.19.4 {
/*import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import com.mojang.datafixers.util.Either;
*///?}

@Mixin(WorldStem.class)
public abstract class WorldStemMixin
{
    //? if >=1.19.4 {
    /*@Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData, CallbackInfo ci)
    {
        var registryAccess = layeredRegistryAccess.compositeAccess();
        //? if >=1.21.2
        /^doReplacement(registryAccess.lookupOrThrow(Registries.BIOME), registryAccess.lookupOrThrow(Registries.LEVEL_STEM));^/
        //? if <1.21.2
        doReplacement(registryAccess.registryOrThrow(Registries.BIOME), registryAccess.registryOrThrow(Registries.LEVEL_STEM));
    }
    
    *///?} else {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, RegistryAccess.Frozen frozen, WorldData worldData, CallbackInfo ci)
    {
        doReplacement(frozen.registryOrThrow(Registry.BIOME_REGISTRY), worldData.worldGenSettings().dimensions());
    }
    //?}
    
    @Unique
    private void doReplacement(Registry<Biome> biomeRegistry, Registry<LevelStem> stemRegistry)
    {
        BiomeReplacer.prepareReplacementRules(biomeRegistry);
        if (BiomeReplacer.noReplacements()) return;
        
        for (var entry : stemRegistry.entrySet())
        {
            var levelId = entry.getKey().location();
            var level = entry.getValue();
            
            if (level.generator() instanceof NoiseBasedChunkGenerator generator
                    && generator.getBiomeSource() instanceof MultiNoiseBiomeSource)
            {
                var biomeSource = (MultiNoiseBiomeSourceAccessor) generator.getBiomeSource();
                
                //? if >=1.19.4
                /*var parameters = biomeSource.getParameters().map((p) -> p, (holder) -> holder.value().parameters());*/
                //? if <1.19.4
                var parameters = biomeSource.getParameters();
                
                List<Pair<Climate.ParameterPoint, Holder<Biome>>> newParameterList = new ArrayList<>();
                for (var value : parameters.values())
                {
                    var newBiome = BiomeReplacer.replaceIfNeeded(value.getSecond());
                    if (newBiome == null) continue;
                    newParameterList.add(new Pair<>(value.getFirst(), newBiome));
                }
                
                //? if >=1.19.4
                /*biomeSource.setParameters(Either.left(new Climate.ParameterList<>(newParameterList)));*/
                //? if <1.19.4
                biomeSource.setParameters(new Climate.ParameterList<>(newParameterList));
                
                BiomeReplacer.log("Successfully replaced biomes in " + levelId);
            }
            else BiomeReplacer.log("Skipping " + levelId);
        }
    }
}
