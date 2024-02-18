package com.GunterPro7.entity;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class MusicBox {
    private final BlockPos blockPos;
    private AudioCable audioCable;
    private boolean powered;

    public MusicBox(BlockPos blockPos) {
        this.blockPos = blockPos;
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

    public void powerDisconnected() {
        powered = false;
        audioCable = null;
    }

    public void powerConnected(AudioCable audioCable) {
        this.audioCable = audioCable;
        powered = true;
    }

    public boolean hasAudioCable() {
        return audioCable != null;
    }
}
