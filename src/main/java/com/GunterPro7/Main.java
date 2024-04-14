package com.GunterPro7;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.listener.*;
import com.GunterPro7.recipe.ModRecipes;
import com.GunterPro7.sound.ModSoundEvents;
import com.GunterPro7.utils.SoundUtils;
import com.GunterPro7.utils.TimeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Random random = new Random();
    @Nullable
    public static MinecraftServer minecraftServer;
    public static boolean serverSide;

    public Main() {
        try {
            Minecraft.getInstance();
        } catch (RuntimeException e) {
            serverSide = true;
        }

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(bus);
        ModItems.register(bus);
        ModRecipes.register(bus);
        ModSoundEvents.register(bus);

        MinecraftForge.EVENT_BUS.register(new AudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicControllerListener());
        MinecraftForge.EVENT_BUS.register(new MusicBoxesCommand());
        MinecraftForge.EVENT_BUS.register(new TimeUtils());

        if (!serverSide) {
            MinecraftForge.EVENT_BUS.register(new AudioCableRenderer());
            MinecraftForge.EVENT_BUS.register(new ClientMusicBoxListener());
        } else {
            MinecraftForge.EVENT_BUS.register(new ServerLoader());
        }

        MusicBoxEvent.INSTANCE.registerMessage(0, MusicBoxEvent.class, MusicBoxEvent::encode, MusicBoxEvent::new, MusicBoxEvent::handle);
        MiscNetworkEvent.INSTANCE.registerMessage(0, MiscNetworkEvent.class, MiscNetworkEvent::encode, MiscNetworkEvent::new, MiscNetworkEvent::handle);

        loadConfigs();
    }

    private void loadConfigs() {
        //ServerMusicBoxListener.musicBoxes.addAll(FileManager.Positions.getAll());
        //FileManager.Controller.getAll().forEach(pos -> MusicController.musicControllers.add(new MusicController(pos)));
    }

    static class ServerLoader {
        @SubscribeEvent
        public void onServerLoad(ServerStartedEvent event) {
            minecraftServer = event.getServer();
        }
    }
}
