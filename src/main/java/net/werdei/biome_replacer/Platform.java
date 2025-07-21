package net.werdei.biome_replacer;

import java.io.File;

import net.werdei.biome_replacer.config.Config;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
*///?} else if oldforge {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
*///?}

//? if forge-like
/*@Mod(value = "biome_replacer")*/
public class Platform /*? if fabric {*/ implements ModInitializer /*?}*/
{
    //? if fabric {
    @Override
    public void onInitialize()
    {
        BiomeReplacer.initialize();
    }
    //?} else {
    /*public Platform()
    {
        BiomeReplacer.initialize();
    }
	*///?}

    public static File getConfigFile()
    {
        //? if fabric
        return FabricLoader.getInstance().getConfigDir().resolve(Config.FILE_NAME).toFile();
        //? if forge-like
        /*return FMLPaths.CONFIGDIR.get().resolve(Config.FILE_NAME).toFile();*/
    }
    
    public static boolean isModLoaded(String modId)
    {
        //? if fabric
        return FabricLoader.getInstance().isModLoaded(modId);
        //? if forge-like {
        /*return ModList.get() != null // ModList can be null if checking too early (like in the MixinPlugin)
                ? ModList.get().isLoaded(modId)
                : LoadingModList.get().getModFileById(modId) != null;
        *///?}
    }
}
