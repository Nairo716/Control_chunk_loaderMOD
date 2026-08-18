package com.nairo.chunkloadermod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockChunkLoader extends BlockContainer {

    private IIcon iconOff;
    private IIcon iconOn;

    public BlockChunkLoader() {
        super(Material.iron);
        setHardness(5.0F);
        setStepSound(soundTypeMetal);
        setBlockName("chunkloader_block");
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityChunkLoader();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (world.isRemote) {
            return;
        }
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChunkLoader) {
            ((TileEntityChunkLoader) te).setActive(powered);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChunkLoader) {
            ((TileEntityChunkLoader) te).setActive(false);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChunkLoader && world.isRemote) {
            Control_chunk_loaderMOD.proxy.openChunkLoaderGui((TileEntityChunkLoader) te);
        }
        return true;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconOff = iconRegister.registerIcon(Control_chunk_loaderMOD.MODID + ":chunkloader_off");
        iconOn = iconRegister.registerIcon(Control_chunk_loaderMOD.MODID + ":chunkloader_on");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityChunkLoader && ((TileEntityChunkLoader) te).isActive()) {
            return iconOn;
        }
        return iconOff;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return iconOff;
    }
}
