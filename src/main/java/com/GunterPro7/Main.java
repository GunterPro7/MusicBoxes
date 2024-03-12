package com.GunterPro7;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.listener.*;
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

        MinecraftForge.EVENT_BUS.register(new ClientAudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ServerAudioCableListener());

        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());
        MinecraftForge.EVENT_BUS.register(new MusicBoxesCommand());

        ClientMusicBoxManager.INSTANCE.registerMessage(0, ClientMusicBoxManager.class, ClientMusicBoxManager::encode, ClientMusicBoxManager::new, ClientMusicBoxManager::handle);

        loadConfigs();
    }

    private void loadConfigs() {
        ServerAudioCableListener.audioCables.addAll(FileManager.AudioCables.getAll());

        for (BlockPos blockPos : FileManager.Positions.getAll()) {
            ServerMusicBoxListener.musicBoxes.add(new MusicBox(blockPos));
        }

        System.out.println("qwheiuqwheiuqwehoiuwqeEHQWUIO");
        System.out.println(ServerAudioCableListener.audioCables);
        System.out.println(ServerMusicBoxListener.musicBoxes);
    }
}
