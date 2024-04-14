package com.GunterPro7.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class MusicTrack {
    private String name;
    private boolean customSound;
    private int lengthInSec;

    public MusicTrack(String name, boolean customSound, int lengthInSec) {
        this.name = name;
        this.customSound = customSound;
        this.lengthInSec = lengthInSec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustomSound() {
        return customSound;
    }

    public void setCustomSound(boolean customSound) {
        this.customSound = customSound;
    }

    public int getLengthInSec() {
        return lengthInSec;
    }

    public int getLengthInTicks() {
        return lengthInSec * 20;
    }

    public void setLengthInSec(int lengthInSec) {
        this.lengthInSec = lengthInSec;
    }

    public boolean switchCustomSound() {
        return customSound = !customSound;
    }

    public ResourceLocation getLocation() {
        return new ResourceLocation(customSound ? "musicboxes:" + name : "minecraft:music_disc." + name);
    }

    @Override
    public String toString() {
        return name + ";" + customSound + ";" + lengthInSec;
    }

    public static MusicTrack fromString(String string) {
        String[] parts = string.split(";");
        return new MusicTrack(parts[0], Boolean.parseBoolean(parts[1]), Integer.parseInt(parts[2]));
    }
}
