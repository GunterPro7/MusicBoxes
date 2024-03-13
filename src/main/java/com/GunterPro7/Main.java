package com.GunterPro7;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.listener.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(bus);
        ModItems.register(bus);

        MinecraftForge.EVENT_BUS.register(new AudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ClientAudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ServerAudioCableListener());

        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new ClientMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicControllerListener());

        MinecraftForge.EVENT_BUS.register(new MusicBoxesCommand());

        ClientMusicBoxManager.INSTANCE.registerMessage(0, ClientMusicBoxManager.class, ClientMusicBoxManager::encode, ClientMusicBoxManager::new, ClientMusicBoxManager::handle);

        loadConfigs();
    }

    private void loadConfigs() {
        ServerAudioCableListener.audioCables.addAll(FileManager.AudioCables.getAll());
        ServerMusicBoxListener.musicBoxes.addAll(FileManager.Positions.getAll());
    }
}
