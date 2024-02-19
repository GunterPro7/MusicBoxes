package com.GunterPro7.main;

import com.GunterPro7.listener.ClientAudioCableListener;
import com.GunterPro7.listener.ClientMusicBoxManager;
import com.GunterPro7.listener.ServerMusicBoxListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";

    public Main() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new ClientAudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());

        ClientMusicBoxManager.INSTANCE.registerMessage(0, ClientMusicBoxManager.class, ClientMusicBoxManager::encode, ClientMusicBoxManager::new, ClientMusicBoxManager::handle);
    }
}
