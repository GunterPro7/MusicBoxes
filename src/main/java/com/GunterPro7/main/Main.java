package com.GunterPro7.main;

import com.GunterPro7.listener.ClientAudioCableListener;
import com.GunterPro7.listener.ClientMusicBoxEvent;
import com.GunterPro7.listener.ServerMusicBoxListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "musicboxes";

    public Main() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new ClientAudioCableListener());
        MinecraftForge.EVENT_BUS.register(new ServerMusicBoxListener());

        ClientMusicBoxEvent.INSTANCE.registerMessage(0, ClientMusicBoxEvent.class, ClientMusicBoxEvent::encode, ClientMusicBoxEvent::new, ClientMusicBoxEvent::handle);
    }
}
