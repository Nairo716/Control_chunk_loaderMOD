package com.nairo.chunkloadermod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import org.lwjgl.opengl.GL11;

public class TileEntityChunkLoader extends TileEntity {

    private Ticket chunkTicket;
    private boolean active = false;
    private RangeMode rangeMode = RangeMode.SIZE_1;

    public boolean isActive() {
        return active;
    }

    public RangeMode getRangeMode() {
        return rangeMode;
    }

    public void setActive(boolean shouldBeActive) {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        if (shouldBeActive == active) {
            return;
        }
        active = shouldBeActive;
        if (active) {
            startLoading();
        } else {
            stopLoading();
        }
        markDirty();
        syncToClient();
    }

    /**
     * GUIから範囲を変更する。稼働中なら古いチケットを解放し新しい範囲で取り直す
     */
    public void setRangeMode(RangeMode newMode) {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        if (newMode == rangeMode) {
            return;
        }
        rangeMode = newMode;
        if (active) {
            stopLoading();
            startLoading();
        }
        markDirty();
        syncToClient();
    }

    private void startLoading() {
        if (chunkTicket != null) {
            return;
        }
        chunkTicket = ForgeChunkManager
            .requestTicket(Control_chunk_loaderMOD.instance, worldObj, ForgeChunkManager.Type.NORMAL);

        if (chunkTicket == null) {
            Control_chunk_loaderMOD.LOG.warn("チャンクローダーのチケット取得に失敗しました at " + xCoord + "," + yCoord + "," + zCoord);
            active = false;
            return;
        }

        NBTTagCompound data = chunkTicket.getModData();
        data.setInteger("x", xCoord);
        data.setInteger("y", yCoord);
        data.setInteger("z", zCoord);

        forceAllChunks(chunkTicket);
    }

    private void stopLoading() {
        if (chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
            chunkTicket = null;
        }
    }

    public void bindRestoredTicket(Ticket ticket) {
        this.chunkTicket = ticket;
        this.active = true;
        forceAllChunks(ticket);
    }

    private void forceAllChunks(Ticket ticket) {
        for (ChunkCoordIntPair pair : rangeMode.getChunksToLoad(xCoord, zCoord)) {
            ForgeChunkManager.forceChunk(ticket, pair);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        stopLoading();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("active", active);
        tag.setInteger("rangeMode", rangeMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("active");
        rangeMode = RangeMode.byOrdinalSafe(tag.getInteger("rangeMode"));
    }

    // ==== クライアントへの状態同期(GUI表示用) ====

    private void syncToClient() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        NBTTagCompound tag = packet.func_148857_g();
        if (tag != null) {
            this.readFromNBT(tag);
        }
    }

    /**
     * ダイヤの剣を手に持っている間、チャンクローダーブロックの位置にビーコン風の光の柱を表示する。
     */
    public static class TileEntityChunkLoaderRenderer extends TileEntitySpecialRenderer {

        private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");

        private static final double BEAM_HEIGHT = 256.0D;
        private static final int SIDES = 4;

        @Override
        public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
            if (!(tileEntity instanceof TileEntityChunkLoader)) {
                return;
            }
            TileEntityChunkLoader te = (TileEntityChunkLoader) tileEntity;

            if (!isHoldingDiamondSword()) {
                return;
            }

            long worldTime = te.getWorldObj() != null ? te.getWorldObj()
                .getTotalWorldTime() : 0L;

            renderBeam(x, y, z, partialTicks, worldTime);
        }

        private boolean isHoldingDiamondSword() {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null) {
                return false;
            }
            ItemStack held = mc.thePlayer.getCurrentEquippedItem();
            return held != null && held.getItem() == Items.diamond_sword;
        }

        private void renderBeam(double x, double y, double z, float partialTicks, long worldTime) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5D, y, z + 0.5D);

            Minecraft.getMinecraft().renderEngine.bindTexture(BEAM_TEXTURE);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);

            // テクスチャを時間経過でスクロールさせ、光が流れる演出にする
            float texOffset = -((worldTime % 256) + partialTicks) / 256.0F;

            double innerRadius = 0.2D;
            double outerRadius = innerRadius * 1.5D;

            // 内側:不透明に近いコアの光
            renderBeamCylinder(innerRadius, texOffset, 1.0F, 1.0F, 1.0F, 1.0F);
            // 外側:半透明のグロー
            renderBeamCylinder(outerRadius, texOffset, 1.0F, 1.0F, 1.0F, 0.3F);

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);

            GL11.glPopMatrix();
        }

        private void renderBeamCylinder(double radius, float texOffset, float r, float g, float b, float a) {
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.setColorRGBA_F(r, g, b, a);

            for (int i = 0; i < SIDES; i++) {
                double angle1 = (Math.PI * 2 * i) / SIDES;
                double angle2 = (Math.PI * 2 * (i + 1)) / SIDES;

                double x1 = Math.sin(angle1) * radius;
                double z1 = Math.cos(angle1) * radius;
                double x2 = Math.sin(angle2) * radius;
                double z2 = Math.cos(angle2) * radius;

                tess.addVertexWithUV(x1, 0, z1, 0, texOffset + 1.0F);
                tess.addVertexWithUV(x1, BEAM_HEIGHT, z1, 0, texOffset);
                tess.addVertexWithUV(x2, BEAM_HEIGHT, z2, 1, texOffset);
                tess.addVertexWithUV(x2, 0, z2, 1, texOffset + 1.0F);
            }

            tess.draw();
        }
    }
}
