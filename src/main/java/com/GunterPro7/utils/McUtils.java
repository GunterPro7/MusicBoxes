package com.GunterPro7.utils;

import com.GunterPro7.Main;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class McUtils {
    public static String getIdentifierByLevel(ServerLevel level) {
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

    public static int getShortDimensionByLevel(ServerLevel level) {
        return switch (level.dimension().location().getPath()) {
            case "overworld" -> 0;
            case "the_nether" -> 1;
            case "the_end" -> 2;
            default -> 3;
        };
    }

    public static Level getLevelByName(String levelName) {
        ResourceKey<Level> dimension = getDimensionByIdentifier(Integer.parseInt(String.valueOf(levelName.charAt(levelName.length() - 1))));
        if (Main.serverSide && Main.minecraftServer != null) {
            for (ServerLevel level : Main.minecraftServer.getAllLevels()) {
                if (getIdentifierByLevel(level).equals(levelName) && dimension.equals(level.dimension())) {
                    return level;
                }
            }
        }

        return null;
    }

    public static float[] getRGB(int color) {
        float[] rgb = new float[3];
        rgb[0] = ((color >> 16) & 0xFF) / 255f;  // Red
        rgb[1] = ((color >> 8) & 0xFF) / 255f;   // Green
        rgb[2] = (color & 0xFF) / 255f;          // Blue
        return rgb;
    }
}
