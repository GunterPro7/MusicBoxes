package com.GunterPro7.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;

import java.util.Objects;

public class MusicBox {
    private final BlockPos blockPos;
    private DyeColor dyeColor;
    private boolean powered;

    public MusicBox(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBox musicBox = (MusicBox) o;
        return blockPos.equals(musicBox.blockPos) && dyeColor == musicBox.dyeColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, dyeColor);
    }

    public boolean isPowered() {
        return powered;
    }

    public void powerDisconnected() {
        powered = false;
    }

    public void powerConnected(DyeColor dyeColor) {
        this.dyeColor = dyeColor;
        powered = true;
    }

}
