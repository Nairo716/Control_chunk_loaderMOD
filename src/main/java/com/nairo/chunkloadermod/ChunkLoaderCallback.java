package com.nairo.chunkloadermod;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkLoaderCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        for (Ticket ticket : tickets) {
            if (!ticket.getModData()
                .hasKey("x")) {
                continue;
            }
            int x = ticket.getModData()
                .getInteger("x");
            int y = ticket.getModData()
                .getInteger("y");
            int z = ticket.getModData()
                .getInteger("z");

            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityChunkLoader) {
                ((TileEntityChunkLoader) te).bindRestoredTicket(ticket);
            } else {
                ForgeChunkManager.releaseTicket(ticket);
            }
        }
    }
}
