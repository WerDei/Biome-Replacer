package net.werdei.biome_replacer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin
{
    private static final Logger LOGGER = LogManager.getLogger(MixinPlugin.class);
    private static final String LOG_PREFIX = "[BiomeReplacer] MixinPlugin: {}";
    private static final String TERRABLENDER_ID = "terrablender";
    private static final String BLUEPRINT_ID = "blueprint";
    private static final String BIOLITH_ID = "biolith";
    private static final String LITHOSTITCHED_ID = "lithostitched";

    private String ownPackage;
    private boolean terrablenderInstalled;
    private boolean blueprintInstalled;
    private boolean biolithInstalled;
    private boolean lithostitchedInstalled;


    @Override
    public void onLoad(String mixinPackage)
    {
        ownPackage = mixinPackage;
        terrablenderInstalled = Platform.isModLoaded(TERRABLENDER_ID);
        blueprintInstalled = Platform.isModLoaded(BLUEPRINT_ID);
        biolithInstalled = Platform.isModLoaded(BIOLITH_ID);
        lithostitchedInstalled = Platform.isModLoaded(LITHOSTITCHED_ID);
        if (terrablenderInstalled)
            LOGGER.info(LOG_PREFIX, "TerraBlender detected, biome replacements will be injected into it");
        if (blueprintInstalled)
            LOGGER.info(LOG_PREFIX, "Blueprint detected, biome replacements will be injected into it");
        if (biolithInstalled)
            LOGGER.info(LOG_PREFIX, "Biolith detected, biome replacements will be injected into it");
        if (lithostitchedInstalled)
            LOGGER.info(LOG_PREFIX, "Lithostitched detected, biome replacements will be registered with it");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        var mixinShortName = mixinClassName.substring(ownPackage.length() + 1);
        if (mixinShortName.startsWith(TERRABLENDER_ID))
            return terrablenderInstalled;
        if (mixinShortName.startsWith(BLUEPRINT_ID))
            return blueprintInstalled;
        if (mixinShortName.startsWith(BIOLITH_ID))
            return biolithInstalled;
        if (mixinShortName.startsWith(LITHOSTITCHED_ID))
            return lithostitchedInstalled;
        return true;
    }
    
    
    // Other methods are irrelevant for us
    
    @Override
    public String getRefMapperConfig()
    {
        return null;
    }
    
    @Override
    public List<String> getMixins()
    {
        return null;
    }
    
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
    
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
