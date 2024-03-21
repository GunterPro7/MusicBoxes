package com.GunterPro7.entity;

import com.GunterPro7.listener.AudioCableListener;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.listener.ServerMusicBoxListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.*;

public class MusicController {
    public static final List<MusicController> musicControllers = new ArrayList<>();
    private final BlockPos pos;
    public final Queue<String> musicQueue = new PriorityQueue<>();

    public MusicController(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public List<AudioCable> getAudioCablesConnected() {
        return AudioCableListener.getAudioCablesByPos(pos);
    }

    @Override
    public String toString() {
        return "Position: " + pos;
    }


    public static MusicController getMusicControllerByMusicBox(MusicBox musicBox) {
        if (musicBox.hasAudioCable()) return getControllerByAudioCable(musicBox.getAudioCable(), new HashSet<>());
        return null;
    }

    public static MusicController getMusicControllerByAudioCable(AudioCable audioCable) {
        return getControllerByAudioCable(audioCable, new HashSet<>());
    }

    public Set<MusicBox> getMusicBoxesByColor(DyeColor dyeColor) {
        return getMusicBoxesByColor(pos, dyeColor, new ArrayList<>());
    }

    private static Set<MusicBox> getMusicBoxesByColor(BlockPos blockPos, DyeColor dyeColor, List<BlockPos> checkedPositions) {
        Set<MusicBox> musicBoxes = new HashSet<>();

        List<AudioCable> audioCables = AudioCableListener.getAudioCablesByPos(blockPos);

        for (AudioCable audioCable : audioCables) {
            if (audioCable.getColor().equals(dyeColor)) {
                if (audioCable.getMusicBoxStart() != null) {
                    musicBoxes.add(audioCable.getMusicBoxStart());
                } if (audioCable.getMusicBoxEnd() != null) {
                    musicBoxes.add(audioCable.getMusicBoxEnd());
                }

                BlockPos newBlockPos;
                if (!checkedPositions.contains(audioCable.getStartBlock())) {
                    newBlockPos = audioCable.getStartBlock() ;
                } else {
                    if (checkedPositions.contains(audioCable.getEndBlock())) {
                        return musicBoxes;
                    }
                    newBlockPos = audioCable.getEndBlock();
                }
                checkedPositions.add(blockPos);

                musicBoxes.addAll(getMusicBoxesByColor(newBlockPos, dyeColor, checkedPositions));
            }
        }

        return musicBoxes;
    }

    private static MusicController getControllerByAudioCable(AudioCable audioCable, Set<BlockPos> checkedPositions) {
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

        List<AudioCable> startBlockCables = AudioCableListener.getAudioCablesByPos(startBlock);
        for (AudioCable cable : startBlockCables) {
            BlockPos pos = cable.getStartBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getControllerByAudioCable(cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        List<AudioCable> endBlockCables = AudioCableListener.getAudioCablesByPos(endBlock);
        for (AudioCable cable : endBlockCables) {
            BlockPos pos = cable.getEndBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getControllerByAudioCable(cable, checkedPositions);
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

    @Deprecated
    public void play(List<ServerPlayer> players, MusicBoxEvent clientMusicBoxManager) {
        players.forEach(player -> ServerMusicBoxListener.sendToClient(player, clientMusicBoxManager));
    }

    public Queue<String> getMusicQueue() {
        return this.musicQueue;
    }
}
