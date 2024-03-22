package com.GunterPro7;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.listener.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.Random;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";
    public static Random random = new Random();
    @Nullable
    public static MinecraftServer minecraftServer;

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(bus);
        ModItems.register(bus);

        MinecraftForge.EVENT_BUS.register(new AudioCableListener());
        MinecraftForge.EVENT_BUS.register(new AudioCableRenderer());

        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new ClientMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicControllerListener());

        MinecraftForge.EVENT_BUS.register(new MusicBoxesCommand());
        MinecraftForge.EVENT_BUS.register(this);

        MusicBoxEvent.INSTANCE.registerMessage(0, MusicBoxEvent.class, MusicBoxEvent::encode, MusicBoxEvent::new, MusicBoxEvent::handle);
        MiscNetworkEvent.INSTANCE.registerMessage(0, MiscNetworkEvent.class, MiscNetworkEvent::encode, MiscNetworkEvent::new, MiscNetworkEvent::handle);

        loadConfigs();
    }

    private void loadConfigs() {
        //ServerMusicBoxListener.musicBoxes.addAll(FileManager.Positions.getAll());
        FileManager.Controller.getAll().forEach(pos -> MusicController.musicControllers.add(new MusicController(pos)));
    }

    private void onServerStarted(ServerStartedEvent event) {
        minecraftServer = event.getServer();
    }
}
