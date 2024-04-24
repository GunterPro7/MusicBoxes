package com.GunterPro7.client;

import com.GunterPro7.ui.MusicBoxesConfigScreen;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientCommandManager {

    @SubscribeEvent
    public void onCommand(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("music_boxes").executes((e) -> MusicBoxesConfigScreen.display()));
    }
}
