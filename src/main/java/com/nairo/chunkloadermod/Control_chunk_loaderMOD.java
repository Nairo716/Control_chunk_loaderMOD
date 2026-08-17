package com.nairo.chunkloadermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
    modid = Control_chunk_loaderMOD.MODID,
    version = Tags.VERSION,
    name = "Control_chunk_loaderMOD",
    acceptedMinecraftVersions = "[1.7.10]")
public class Control_chunk_loaderMOD {

    public static final String MODID = "controlchunkloader";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static Control_chunk_loaderMOD instance;

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @SidedProxy(
        clientSide = "com.nairo.chunkloadermod.ClientProxy",
        serverSide = "com.nairo.chunkloadermod.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
