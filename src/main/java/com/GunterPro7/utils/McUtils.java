package com.GunterPro7.utils;

import com.GunterPro7.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class McUtils {
    // Does not work in single player worlds
    public static boolean isServerSide() {
        return Minecraft.getInstance().player == null;
    }

    public static boolean isSinglePlayer() {
        return Minecraft.getInstance().isSingleplayer();
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

    public static Level getLevelByName(String levelName) {
        String name = levelName.substring(0, levelName.length() - 1);
        ResourceKey<Level> dimension = getDimensionByIdentifier(levelName.charAt(levelName.length() - 1));
        if (isServerSide()) {
            for (Level level : Main.minecraftServer.getAllLevels()) {
                if (getIdentifierByLevel(level).equals(name) && dimension.equals(level.dimension())) {
                    return level;
                }
            }
        } else {
            return Minecraft.getInstance().level;
        }

        return null;
    }

    public static String getIdentifierByLevel(Level level) {
        String part = level.toString().split("\\[")[0]; // TODO somehow its the wrong name of the toString :O
        return part.substring(0, part.length() - 1) + getShortDimensionByLevel(level);
    }

    public static ResourceKey<Level> getDimensionByIdentifier(int identifier) {
        return switch (identifier) {
            case 0 -> Level.OVERWORLD;
            case 1 -> Level.NETHER;
            case 2 -> Level.END;
            default -> null;
        };
    }

    public static int getShortDimensionByLevel(Level level) {
        return switch (level.dimension().registry().getPath()) {
            case "overworld" -> 0;
            case "the_nether" -> 1;
            case "the_end" -> 2;
            default -> 3;
        };
    }
}
