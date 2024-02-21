package com.GunterPro7.entity;

import com.GunterPro7.listener.ClientMusicBoxManager;
import com.GunterPro7.listener.ServerMusicBoxListener;
import com.GunterPro7.utils.JsonUtils;
import com.GunterPro7.utils.Utils;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AudioCable {
    private Vec3 startPos;
    private Vec3 endPos;
    private BlockPos startBlock;
    private BlockPos endBlock;
    private Vec3 centerPos;
    private DyeColor color;

    @Nullable
    private MusicBox musicBoxStart;
    @Nullable
    private MusicBox musicBoxEnd;

    private final VertexBuffer vertexBuffer;

    public AudioCable(Vec3 startPos, Vec3 endPos, BlockPos startBlock, BlockPos endBlock) {
        this(startPos, endPos, startBlock, endBlock, DyeColor.WHITE);
    }

    public AudioCable(Vec3 startPos, Vec3 endPos, BlockPos startBlock, BlockPos endBlock, DyeColor color) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.startBlock = startBlock;
        this.endBlock = endBlock;
        this.color = color;
        this.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);


        // x1 = 200, x2 = 220    -> mid => 210  ->
        // x1 = 220, x2 = 200    -> mid => 210  ->
        double x = Utils.getCenter(startPos.x(), endPos.x());
        double y = Utils.getCenter(startPos.y(), endPos.y());
        double z = Utils.getCenter(startPos.z(), endPos.z());
        this.centerPos = new Vec3(x, y, z);
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public Vec3 getStartPos() {
        return startPos;
    }

    public Vec3 getEndPos() {
        return endPos;
    }

    public BlockPos getStartBlock() {
        return startBlock;
    }

    public BlockPos getEndBlock() {
        return endBlock;
    }

    public DyeColor getColor() {
        return color;
    }

    @Nullable
    public MusicBox getMusicBoxStart() {
        return ServerMusicBoxListener.getMusicBoxByPos(startBlock);
    }

    @Nullable
    public MusicBox getMusicBoxEnd() {
        return ServerMusicBoxListener.getMusicBoxByPos(endBlock);
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
        return ("startPos:" + startPos + ";endPos:" + endPos + ";startBlock:" + startBlock.toShortString() + ";endBlock:" + endBlock.toShortString() + ";color:" + color.name()).replaceAll("[()]", "");
    }

    public static AudioCable fromString(String audioCableString) {
        Map<String, String> map = JsonUtils.asMap(audioCableString);

    return new AudioCable(Utils.vec3Of(map.get("startPos")), Utils.vec3Of(map.get("endPos")), Utils.blockPosOf(map.get("startBlock")), Utils.blockPosOf(map.get("endBlock")), DyeColor.valueOf(map.get("color")));
    }
}
