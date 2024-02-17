package com.GunterPro7.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatUtils {
    public static void sendPrivateChatMessage(String msg) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null)
            localPlayer.sendSystemMessage(Component.literal(msg));
    }
}
