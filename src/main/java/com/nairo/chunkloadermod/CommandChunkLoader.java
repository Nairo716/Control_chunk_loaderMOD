package com.nairo.chunkloadermod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

public class CommandChunkLoader extends CommandBase {

    @Override
    public String getCommandName() {
        return "chunkloader";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/chunkloader <on|off> <x> <y> <z>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 4) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        boolean turnOn = args[0].equalsIgnoreCase("on");
        int x = parseInt(sender, args[1]);
        int y = parseInt(sender, args[2]);
        int z = parseInt(sender, args[3]);

        TileEntity te = sender.getEntityWorld()
            .getTileEntity(x, y, z);
        if (te instanceof TileEntityChunkLoader) {
            ((TileEntityChunkLoader) te).setActive(turnOn);
            sender.addChatMessage(new ChatComponentText("チャンクローダーを" + (turnOn ? "ON" : "OFF") + "にしました。"));
        } else {
            sender.addChatMessage(new ChatComponentText("指定座標にチャンクローダーが見つかりません。"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
