package com.GunterPro7.utils;

import com.GunterPro7.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class McUtils {
    public static final Map<String, RecordItem> MUSIC_DISCS = MapUtils.of(
            "minecraft:music_disc.5", Items.MUSIC_DISC_5,
            "minecraft:music_disc.11", Items.MUSIC_DISC_11,
            "minecraft:music_disc.13", Items.MUSIC_DISC_13,
            "minecraft:music_disc.blocks", Items.MUSIC_DISC_BLOCKS,
            "minecraft:music_disc.cat", Items.MUSIC_DISC_CAT,
            "minecraft:music_disc.chirp", Items.MUSIC_DISC_CHIRP,
            "minecraft:music_disc.far", Items.MUSIC_DISC_FAR,
            "minecraft:music_disc.mall", Items.MUSIC_DISC_MALL,
            "minecraft:music_disc.mellohi", Items.MUSIC_DISC_MELLOHI,
            "minecraft:music_disc.otherside", Items.MUSIC_DISC_OTHERSIDE,
            "minecraft:music_disc.pigstep", Items.MUSIC_DISC_PIGSTEP,
            "minecraft:music_disc.relic", Items.MUSIC_DISC_RELIC,
            "minecraft:music_disc.stal", Items.MUSIC_DISC_STAL,
            "minecraft:music_disc.strad", Items.MUSIC_DISC_STRAD,
            "minecraft:music_disc.wait", Items.MUSIC_DISC_WAIT,
            "minecraft:music_disc.ward", Items.MUSIC_DISC_WARD
    );

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

    public static Resource getInputStreamOfLocation(ResourceLocation location) {
        MinecraftServer server = Main.getServer();

        if (server != null) {
            ResourceManager manager = server.getResourceManager();

            Optional<Resource> resource = manager.getResource(new ResourceLocation("musicboxes", "../../assets/musicboxes/sounds/custom1.ogg"));
            if (resource.isPresent()) {
                return manager.getResource(location).get();
            }
        }
        return null;
    }
}
