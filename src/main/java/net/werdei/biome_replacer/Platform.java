package net.werdei.biome_replacer;

import java.io.File;

import net.werdei.biome_replacer.config.Config;
//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
*///?} else if lexforge {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
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
    //?} else if forge-like {
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
}
