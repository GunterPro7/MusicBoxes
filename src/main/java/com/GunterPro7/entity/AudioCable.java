package com.GunterPro7.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;

import java.util.Objects;

public class AudioCable {
    private final BlockPos startPos;
    private final BlockPos endPos;
    private DyeColor color;

    public AudioCable(BlockPos startPos, BlockPos endPos) {
        this(startPos, endPos, DyeColor.WHITE);
    }

    public AudioCable(BlockPos startPos, BlockPos endPos, DyeColor color) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.color = color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public BlockPos getEndPos() {
        return endPos;
    }

    public DyeColor getColor() {
        return color;
    }

    public float[] getRGB() {
        return color.getTextureDiffuseColors();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioCable that = (AudioCable) o;
        return Objects.equals(startPos, that.startPos) && Objects.equals(endPos, that.endPos) && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos, color);
    }
}
