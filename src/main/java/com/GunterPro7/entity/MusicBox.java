package com.GunterPro7.entity;

import com.GunterPro7.block.MusicBoxBlock;
import com.GunterPro7.listener.AudioCableListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class MusicBox {
    private final BlockPos blockPos;
    private final Level level;
    private AudioCable audioCable;
    private boolean powered;

    public MusicBox(BlockPos blockPos, Level level) {
        if (!(level instanceof ServerLevel)) {
            throw new IllegalArgumentException("Level has to be a ServerLevel instance.");
        }
        this.blockPos = blockPos;
        this.level = level;
        List<AudioCable> cables = AudioCableListener.getAudioCablesByPos(blockPos);
        if (cables.size() > 0) {
            this.audioCable = cables.get(0);
            this.powered = true;
        }
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
        return !AudioCableListener.getAudioCablesByPos(blockPos).isEmpty();
    }

    public boolean isActive() {
        return this.level.getBlockState(blockPos).getValue(MusicBoxBlock.ACTIVE);
    }

    public void setActive(boolean active) {
        this.level.setBlock(blockPos, this.level.getBlockState(blockPos).setValue(MusicBoxBlock.ACTIVE, active), 1);
    }

    public void powerDisconnected() {
        powered = false;
        audioCable = null;
    }

    public void powerConnected(AudioCable audioCable) {
        this.audioCable = audioCable;
        powered = true;
    }

    public float getVolume() {
        return (float) this.level.getBlockState(blockPos).getValue(MusicBoxBlock.VOLUME);
    }

    public void setVolume(float volume) {
        this.level.setBlock(blockPos, this.level.getBlockState(blockPos).setValue(MusicBoxBlock.VOLUME, Math.round(volume)), 1);
    }

    public boolean hasAudioCable() {
        return audioCable != null;
    }

    @Override
    public String toString() {
        return "MusicBox at: " + blockPos + ", AudioCable connected: " + (audioCable != null ? audioCable : "null") + "";
    }

}
