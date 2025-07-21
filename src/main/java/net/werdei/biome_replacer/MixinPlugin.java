package net.werdei.biome_replacer;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin
{
    private static final String TERRABLENDER_ID = "terrablender";
    private String ownPackage;
    private boolean terrablenderInstalled;
    
    @Override
    public void onLoad(String mixinPackage)
    {
        ownPackage = mixinPackage;
        terrablenderInstalled = Platform.isModLoaded(TERRABLENDER_ID);
        if (terrablenderInstalled)
            BiomeReplacer.log("TerraBlender detected, biome replacements will be injected into it");
    }
    
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        var mixinShortName = mixinClassName.substring(ownPackage.length() + 1);
        if (mixinShortName.startsWith(TERRABLENDER_ID))
            return terrablenderInstalled;
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
