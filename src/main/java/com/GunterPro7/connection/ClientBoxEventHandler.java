package com.GunterPro7.connection;

import com.GunterPro7.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class ClientBoxEventHandler {
    @OnlyIn(Dist.CLIENT)
    private static final List<SoundInstance> instances = new ArrayList<>();
    @OnlyIn(Dist.CLIENT)
    private static final Minecraft mc = Minecraft.getInstance();

    @OnlyIn(Dist.CLIENT)
    static void removeInactiveSounds() {
        SoundManager soundManager = mc.getSoundManager();
        instances.removeIf(soundInstance -> !soundManager.isActive(soundInstance));
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList) {
        removeInactiveSounds();

        SoundManager soundManager = mc.getSoundManager();
        for (SoundInstance soundInstance : instances) {
            if (resourceLocation == null || soundInstance.getLocation().equals(resourceLocation)) {
                BlockPos blockPos = new BlockPos((int) Math.floor(soundInstance.getX()), (int) Math.floor(soundInstance.getY()), (int) Math.floor(soundInstance.getZ()));
                if (blockPosList.contains(blockPos)) {
                    soundManager.stop(soundInstance);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList, List<Float> volumeList) {
        ClientBoxEventHandler.removeInactiveSounds();

        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);

        for (int i = 0; i < blockPosList.size(); i++) {
            BlockPos pos = blockPosList.get(i);
            float volume = volumeList.get(i);


            double v;
            try {
                v = Double.parseDouble(Main.fileManager.valueByKeyAndName("config.txt", "musicBoxLoudness"));
            } catch (NumberFormatException e) {
                v = 25;
            }

            playSound(soundEvent, pos, (float) (volume / 75 * v / 25));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent soundEvent, BlockPos pos, float volume) {
        SoundInstance soundInstance = new SimpleSoundInstance(soundEvent, SoundSource.RECORDS, volume, 1f, SoundInstance.createUnseededRandom(), pos);
        instances.add(soundInstance);

        mc.getSoundManager().playDelayed(soundInstance, 2);
    }
}
