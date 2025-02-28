package net.werdei.biome_replacer.mixin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.storage.WorldData;
import net.werdei.biome_replacer.BiomeReplacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldStem.class)
public abstract class WorldStemMixin
{
    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onStemCreated(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData, CallbackInfo ci)
    {
        BiomeReplacer.prepareReplacementRules(registries());
        if (BiomeReplacer.noReplacements()) return;

        registries().compositeAccess().lookupOrThrow(Registries.LEVEL_STEM).listElements().forEach(level ->
        {
            var levelId = level.key().location();
            if (level.value().generator() instanceof NoiseBasedChunkGenerator generator
                    && generator.getBiomeSource() instanceof MultiNoiseBiomeSource)
            {
                var biomeSource = (MultiNoiseBiomeSourceAccessor) generator.getBiomeSource();
                var parameters = biomeSource.getParameters().map((p) -> p, (holder) -> holder.value().parameters());
                
                List<Pair<Climate.ParameterPoint, Holder<Biome>>> newParameterList = new ArrayList<>();
                for (var value : parameters.values())
                {
                    var newBiome = BiomeReplacer.replaceIfNeeded(value.getSecond());
                    if (newBiome == null) continue;
                    newParameterList.add(new Pair<>(value.getFirst(), newBiome));
                }
                biomeSource.setParameters(Either.left(new Climate.ParameterList<>(newParameterList)));
                
                BiomeReplacer.log("Successfully replaced biomes in " + levelId);
            }
            else BiomeReplacer.log("Skipping " + levelId);
        });
    }
}
