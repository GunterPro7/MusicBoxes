package com.GunterPro7.entity;

import com.GunterPro7.listener.ClientAudioCableListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicController {
    private static final List<MusicController> musicControllers = List.of(new MusicController(new BlockPos(0, 100, 0)));
    private final BlockPos pos;

    public MusicController(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "Position: " + pos;
    }


    public static MusicController getMusicControllerByMusicBox(MusicBox musicBox) {
        if (musicBox.hasAudioCable()) return getBlockPosOfMusicControllerByAudioCable(musicBox.getAudioCable(), new HashSet<>());

        MusicController musicController = getBlockPosOfMusicControllerByAudioCable(musicBox.getAudioCable(), new HashSet<>());
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(musicController != null ? musicController.toString() : "Null sadly"));

        return null;
    }

    public static MusicController getMusicControllerByAudioCable(AudioCable audioCable) {
        return getBlockPosOfMusicControllerByAudioCable(audioCable, new HashSet<>());
    }

    private static MusicController getBlockPosOfMusicControllerByAudioCable(AudioCable audioCable, Set<BlockPos> checkedPositions) {
        BlockPos startBlock = audioCable.getStartBlock();
        BlockPos endBlock = audioCable.getEndBlock();

        MusicController musicControllerStart = MusicController.getController(startBlock);
        if (musicControllerStart != null) {
            return musicControllerStart;
        }

        MusicController musicControllerEnd = MusicController.getController(endBlock);
        if (musicControllerEnd != null) {
            return musicControllerEnd;
        }

        checkedPositions.add(startBlock);
        checkedPositions.add(endBlock);

        List<AudioCable> startBlockCables = ClientAudioCableListener.getAudioCablesByPos(startBlock);
        for (AudioCable cable : startBlockCables) {
            BlockPos pos = cable.getStartBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getBlockPosOfMusicControllerByAudioCable(cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        List<AudioCable> endBlockCables = ClientAudioCableListener.getAudioCablesByPos(endBlock);
        for (AudioCable cable : endBlockCables) {
            BlockPos pos = cable.getEndBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getBlockPosOfMusicControllerByAudioCable(cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        return null;
    }

    public static MusicController getController(BlockPos pos) {
        for (MusicController mc : musicControllers) {
            if (pos.equals(mc.getPos())) {
                return mc;
            }
        }
        return null;
    }
}