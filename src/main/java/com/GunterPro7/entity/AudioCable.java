package com.GunterPro7.entity;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class AudioCable {
    private final BlockPos startPos;
    private final BlockPos endPos;
    private final BlockPos centerPos;
    private DyeColor color;

    private final VertexBuffer vertexBuffer;

    public AudioCable(BlockPos startPos, BlockPos endPos) {
        this(startPos, endPos, DyeColor.WHITE);
    }

    public AudioCable(BlockPos startPos, BlockPos endPos, DyeColor color) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.color = color;
        this.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        double x = startPos.getX() - ((double) (startPos.getX() - endPos.getX()) / 2);
        double y = startPos.getY() - ((double) (startPos.getY() - endPos.getY()) / 2);
        double z = startPos.getZ() - ((double) (startPos.getZ() - endPos.getZ()) / 2); // TODO this calculation is wrong
        this.centerPos = new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
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

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioCable that = (AudioCable) o;
        return startPos.equals(that.startPos) && endPos.equals(that.endPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos);
    }

    public double getDistance(Vec3 position) {
        return centerPos.distToCenterSqr(position);
    }

    public boolean isInRange(Vec3 position, double range) {
        return centerPos.closerToCenterThan(position, range);
    }
}
