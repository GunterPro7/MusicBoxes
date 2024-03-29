package com.GunterPro7.entity;

import com.GunterPro7.Main;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.item.MusicCableItem;
import com.GunterPro7.listener.ServerMusicBoxListener;
import com.GunterPro7.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class AudioCable {
    private final Vec3 startPos;
    private final Vec3 endPos;
    private final BlockPos startBlock;
    private final BlockPos endBlock;
    private final Vec3 centerPos;
    private final Level level;
    private int color;

    public AudioCable(Vec3 startPos, Vec3 endPos, BlockPos startBlock, BlockPos endBlock, Level level) {
        this(startPos, endPos, startBlock, endBlock, level, 0xFFFFFF);
    }

    public AudioCable(Vec3 startPos, Vec3 endPos, BlockPos startBlock, BlockPos endBlock, Level level, int color) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.startBlock = startBlock;
        this.endBlock = endBlock;
        this.color = color;
        this.level = level;

        // x1 = 200, x2 = 220    -> mid => 210  ->
        // x1 = 220, x2 = 200    -> mid => 210  ->
        double x = Utils.getCenter(startPos.x(), endPos.x());
        double y = Utils.getCenter(startPos.y(), endPos.y());
        double z = Utils.getCenter(startPos.z(), endPos.z());
        this.centerPos = new Vec3(x, y, z);
    }

    public void drop() {
        ItemStack itemStack = new ItemStack(ModItems.MUSIC_CABLE_ITEM.get(), 1);
        ((MusicCableItem) itemStack.getItem()).setColor(itemStack, this.color);
        ItemEntity itemEntity = new ItemEntity(level, centerPos.x(), centerPos.y(), centerPos.z(), itemStack);

        level.addFreshEntity(itemEntity);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Vec3 getStartPos() {
        return startPos;
    }

    public Vec3 getEndPos() {
        return endPos;
    }

    public Vec3 getCenterPos() {
        return centerPos;
    }

    public AABB getBoundingBox(double boundingArea) {
        return new AABB(centerPos.x - boundingArea, centerPos.y - boundingArea, centerPos.z - boundingArea,
                centerPos.x + boundingArea, centerPos.y + boundingArea, centerPos.z + boundingArea);
    }

    public BlockPos getStartBlock() {
        return startBlock;
    }

    public BlockPos getEndBlock() {
        return endBlock;
    }

    public Level getLevel() {
        return level;
    }

    public int getColor() {
        return color;
    }

    @Nullable
    public MusicBox getMusicBoxStart() {
        return ServerMusicBoxListener.getMusicBoxByPos(level, startBlock);
    }

    @Nullable
    public MusicBox getMusicBoxEnd() {
        return ServerMusicBoxListener.getMusicBoxByPos(level, endBlock);
    }

    public float[] getRGB() {
        return McUtils.getRGB(color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioCable that = (AudioCable) o;
        return startPos.equals(that.startPos) && endPos.equals(that.endPos) && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos, color);
    }

    public double getDistance(Vec3 position) {
        return centerPos.distanceToSqr(position);
    }

    public double getBlockDistance() {
        return startPos.distanceTo(endPos);
    }

    public boolean isInRange(Vec3 position, double range) {
        return centerPos.closerThan(position, range);
    }

    @Override
    public String toString() {
        return ("startPos:" + startPos + ";endPos:" + endPos + ";startBlock:" + startBlock.toShortString().replaceAll(", ", ",")
                + ";endBlock:" + endBlock.toShortString().replaceAll(", ", ",") + ";id:" +
                (Main.serverSide ? McUtils.getIdentifierByLevel((ServerLevel) level) : ClientUtils.getIdentifierByLevel(level))
                + ";color:" + color).replaceAll("[()]", "");
    }

    public static AudioCable fromString(String string) {
        Map<String, String> map = JsonUtils.asMap(string);

        return new AudioCable(Utils.vec3Of(map.get("startPos")), Utils.vec3Of(map.get("endPos")),
                Utils.blockPosOf(map.get("startBlock")), Utils.blockPosOf(map.get("endBlock")), ClientUtils.getLevelByName(map.get("id")), Integer.parseInt(map.get("color")));
    }
}
