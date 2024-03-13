package com.GunterPro7.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class McUtils {
    public static boolean isServerSide() {
        return Minecraft.getInstance().player == null;
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendPrivateChatMessage(String msg) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null)
            localPlayer.sendSystemMessage(Component.literal(msg));
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean gameLoaded() {
        return Minecraft.getInstance().player != null; // TODO this is not true server side
    }
}
