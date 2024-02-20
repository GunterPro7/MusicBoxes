package com.GunterPro7.entity;

import com.GunterPro7.listener.AudioCableListener;
import com.GunterPro7.listener.ClientAudioCableListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;

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

    public static Set<MusicBox> getMusicBoxesByColorAndPos(BlockPos pos, DyeColor dyeColor) {
        return getMusicBoxesByColor(pos, dyeColor, new ArrayList<>());
    }

    // TODO untested
    private static Set<MusicBox> getMusicBoxesByColor(BlockPos blockPos, DyeColor dyeColor, List<BlockPos> checkedPositions) {
        Set<MusicBox> musicBoxes = new HashSet<>();

        List<AudioCable> audioCables = AudioCableListener.getAudioCablesByPos(blockPos);

        for (AudioCable audioCable : audioCables) {
            if (audioCable.getColor().equals(dyeColor)) {
                if (audioCable.getMusicBoxStart() != null) {
                    musicBoxes.add(audioCable.getMusicBoxStart());
                } else if (audioCable.getMusicBoxEnd() != null) {
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
                checkedPositions.add(newBlockPos);

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

        List<AudioCable> startBlockCables = ClientAudioCableListener.getAudioCablesByPos(startBlock);
        for (AudioCable cable : startBlockCables) {
            BlockPos pos = cable.getStartBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getControllerByAudioCable(cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        List<AudioCable> endBlockCables = ClientAudioCableListener.getAudioCablesByPos(endBlock);
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

    // TODO s:
    // Saving audio Cables to file
    // Saving music Controller to file
    // check if there are any problmes with playing music when the cables are placed in different directions etc
    // There are some errors like some music boxes are not playing, checking wenn ich wieder bock hab
}
