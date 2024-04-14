package com.GunterPro7.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientUtils {
    public static Level getLevelByName(String levelName) {
        ResourceKey<Level> dimension = getDimensionByIdentifier(Integer.parseInt(String.valueOf(levelName.charAt(levelName.length() - 1))));
        if (isSinglePlayer()) {
            for (Level level : Minecraft.getInstance().getSingleplayerServer().getAllLevels()) {
                if (getIdentifierByLevel(level).equals(levelName) && dimension.equals(level.dimension())) {
                    return level;
                }
            }
        } else {
            return Minecraft.getInstance().level;
        }

        return null;
    }

    public static String getIdentifierByLevel(Level level) {
        return (level.toString().contains("[") ? level.toString().split("\\[")[1].
                replaceAll("]", "") : level.toString()) + getShortDimensionByLevel(level);
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
        return switch (level.dimension().location().getPath()) {
            case "overworld" -> 0;
            case "the_nether" -> 1;
            case "the_end" -> 2;
            default -> 3;
        };
    }

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
        return Minecraft.getInstance().player != null;
    }

    @OnlyIn(Dist.CLIENT)
    public static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    @OnlyIn(Dist.CLIENT)
    public static Level level() {
        return player().level();
    }
}
