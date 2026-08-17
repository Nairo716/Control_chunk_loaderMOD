package com.nairo.chunkloadermod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSetRange implements IMessage {

    private int x, y, z;
    private int rangeOrdinal;

    public MessageSetRange() {}

    public MessageSetRange(int x, int y, int z, int rangeOrdinal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rangeOrdinal = rangeOrdinal;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        rangeOrdinal = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(rangeOrdinal);
    }

    public static class Handler implements IMessageHandler<MessageSetRange, IMessage> {

        @Override
        public IMessage onMessage(MessageSetRange message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityChunkLoader) {
                ((TileEntityChunkLoader) te).setRangeMode(RangeMode.byOrdinalSafe(message.rangeOrdinal));
            }
            return null;
        }
    }
}
