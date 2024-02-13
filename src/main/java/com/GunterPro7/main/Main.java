package com.GunterPro7.main;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        //ModItems.register(bus);
        //ModBlocks.register(bus);

        MinecraftForge.EVENT_BUS.register(new MusicBox());
    }
}
