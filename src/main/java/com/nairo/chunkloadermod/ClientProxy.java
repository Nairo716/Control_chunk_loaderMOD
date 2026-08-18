package com.nairo.chunkloadermod;

public class ClientProxy extends CommonProxy {

    @Override
    public void openChunkLoaderGui(TileEntityChunkLoader te) {
        net.minecraft.client.Minecraft.getMinecraft()
            .displayGuiScreen(new GuiChunkLoader(te));
    }
}
