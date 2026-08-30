package com.nairo.chunkloadermod;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityChunkLoader.class,
            new TileEntityChunkLoader.TileEntityChunkLoaderRenderer());
    }

    @Override
    public void openChunkLoaderGui(TileEntityChunkLoader te) {
        net.minecraft.client.Minecraft.getMinecraft()
            .displayGuiScreen(new GuiChunkLoader(te));
    }
}
