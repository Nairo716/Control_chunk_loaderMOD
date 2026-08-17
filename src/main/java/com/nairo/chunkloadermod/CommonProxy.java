package com.nairo.chunkloadermod;

import net.minecraftforge.common.ForgeChunkManager;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public static BlockChunkLoader blockChunkLoader;

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Control_chunk_loaderMOD.LOG.info(Config.greeting);
        Control_chunk_loaderMOD.LOG.info("I am MyMod at version " + Tags.VERSION);

        // ワールド再読み込み時にチケットを復元するためのコールバック登録
        ForgeChunkManager.setForcedChunkLoadingCallback(Control_chunk_loaderMOD.instance, new ChunkLoaderCallback());
    }

    public void init(FMLInitializationEvent event) {
        blockChunkLoader = new BlockChunkLoader();
        GameRegistry.registerBlock(blockChunkLoader, "chunkloader_block");
        GameRegistry.registerTileEntity(TileEntityChunkLoader.class, Control_chunk_loaderMOD.MODID + "_chunkloader_te");
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChunkLoader());
    }
}
