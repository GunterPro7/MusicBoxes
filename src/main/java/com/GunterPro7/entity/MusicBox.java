package com.GunterPro7.entity;

import com.GunterPro7.listener.ServerAudioCableListener;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

public class MusicBox {
    private final BlockPos blockPos;
    private AudioCable audioCable;
    private double volume = 0.5d;
    private boolean powered;
    private boolean active;

    public MusicBox(BlockPos blockPos, double volume, boolean active) {
        this.blockPos = blockPos;
        List<AudioCable> cables = ServerAudioCableListener.getAudioCablesByPos(blockPos);
        if (cables.size() > 0) {
            this.audioCable = cables.get(0);
            this.powered = true;
        }

        this.active = active;
    }

    public MusicBox(BlockPos blockPos) {
        this(blockPos, 50d, true);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public AudioCable getAudioCable() {
        return audioCable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBox musicBox = (MusicBox) o;
        return blockPos.equals(musicBox.blockPos) && audioCable == musicBox.audioCable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, audioCable);
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void powerDisconnected() {
        powered = false;
        audioCable = null;
    }

    public void powerConnected(AudioCable audioCable) {
        this.audioCable = audioCable;
        powered = true;
    }

    public double getVolume() {
        return this.volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public boolean hasAudioCable() {
        return audioCable != null;
    }

    @Override
    public String toString() {
        return "MusicBox at: " + blockPos + ", AudioCable connected: " + (audioCable != null ? audioCable : "null") + "";
    }
}
