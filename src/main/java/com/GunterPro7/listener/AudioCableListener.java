package com.GunterPro7.listener;

import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.utils.ChatUtils;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// Client & Server Side
public class AudioCableListener {
    protected static final Set<AudioCable> audioCables = new CopyOnWriteArraySet<>();
    @Nullable
    protected Vec3 pos1;
    protected long timePos1;
    @Nullable
    protected BlockPos block1;

    protected static VertexBuffer vertexBuffer;

    public static List<AudioCable> getAudioCablesByPos(BlockPos pos) {
        List<AudioCable> newAudioCables = new ArrayList<>();
        for (AudioCable audioCable : audioCables) {
            if (pos.equals(audioCable.getStartBlock()) || pos.equals(audioCable.getEndBlock())) {
                newAudioCables.add(audioCable);
            }
        }
        return newAudioCables;
    }
}
