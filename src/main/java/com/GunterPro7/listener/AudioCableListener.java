package com.GunterPro7.listener;

import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.main.FileManager;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Client & Server Side
public class AudioCableListener {
    public static final List<AudioCable> audioCables = FileManager.AudioCables.getAll();
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
