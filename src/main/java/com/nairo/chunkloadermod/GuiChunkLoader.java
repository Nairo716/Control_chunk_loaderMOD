package com.nairo.chunkloadermod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiChunkLoader extends GuiScreen {

    private final TileEntityChunkLoader tileEntity;

    public GuiChunkLoader(TileEntityChunkLoader tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int centerX = width / 2;
        int startY = height / 2 - 30;
        int buttonWidth = 90;
        int buttonHeight = 20;
        int gap = 4;

        RangeMode[] modes = RangeMode.values();
        for (int i = 0; i < modes.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int bx = centerX - buttonWidth - gap / 2 + col * (buttonWidth + gap);
            int by = startY + row * (buttonHeight + gap);
            buttonList.add(new GuiButton(i, bx, by, buttonWidth, buttonHeight, modes[i].displayName));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 0 && button.id < RangeMode.values().length) {
            Control_chunk_loaderMOD.NETWORK
                .sendToServer(new MessageSetRange(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, button.id));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "チャンクローダー設定", width / 2, height / 2 - 60, 0xFFFFFF);

        String status = "状態: " + (tileEntity.isActive() ? "ON" : "OFF");
        drawCenteredString(
            fontRendererObj,
            status,
            width / 2,
            height / 2 - 48,
            tileEntity.isActive() ? 0x55FF55 : 0xFF5555);

        String range = "現在の範囲: " + tileEntity.getRangeMode().displayName;
        drawCenteredString(fontRendererObj, range, width / 2, height / 2 - 36, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
