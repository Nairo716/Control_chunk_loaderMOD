package com.nairo.chunkloadermod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

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
}
