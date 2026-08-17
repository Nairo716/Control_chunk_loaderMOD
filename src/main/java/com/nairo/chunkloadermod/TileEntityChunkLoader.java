package com.nairo.chunkloadermod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class TileEntityChunkLoader extends TileEntity {

    private Ticket chunkTicket;
    private boolean active = false;

    public boolean isActive() {
        return active;
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
        int radius = Config.loadRadiusBlocks;

        int minChunkX = (xCoord - radius) >> 4;
        int maxChunkX = (xCoord + radius) >> 4;
        int minChunkZ = (zCoord - radius) >> 4;
        int maxChunkZ = (zCoord + radius) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(cx, cz));
            }
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
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("active");
    }
}
