package com.GunterPro7.entity;

import com.GunterPro7.listener.AudioCableListener;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.listener.ServerMusicBoxListener;
import com.GunterPro7.ui.MusicControllerScreen;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class MusicController {
    public static final List<MusicController> musicControllers = new ArrayList<>(); // TODO instead of writing it into external files, save it into the basic minecraft file
    private final BlockPos pos;
    public final MusicQueue musicQueue;

    public MusicController(BlockPos pos) {
        this(pos, new MusicQueue());
    }

    public MusicController(BlockPos pos, MusicQueue queue) {
        this.pos = pos;
        this.musicQueue = queue;
    }

    public BlockPos getPos() {
        return pos;
    }

    public List<AudioCable> getAudioCablesConnected() {
        return AudioCableListener.getAudioCablesByPos(pos);
    }

    public Set<Integer> getColorsConnected() {
        Set<Integer> colorsConnected = new HashSet<>();
        for (AudioCable audioCable : getAudioCablesConnected()) {
            colorsConnected.add(audioCable.getColor());
        }

        return colorsConnected;
    }

    public Map<Integer, Boolean> getColorInfos() { // TODO das von der config vom block lesen
        Map<Integer, Boolean> colorsConnected = new HashMap<>();
        for (AudioCable audioCable : getAudioCablesConnected()) {
            colorsConnected.put(audioCable.getColor(), true);
        }

        return colorsConnected;
    }

    public boolean switchColorConnection(int color) {
        // TODO remove item metadata for "color" if "color" is already set. Add it if it cannot be found
        return new Random().nextBoolean();
    }

    public boolean isColorConnectionActive(int color) {
        return new Random().nextBoolean();
    }

    public static MusicController getMusicControllerByMusicBox(MusicBox musicBox) {
        if (musicBox.hasAudioCable()) return getControllerByAudioCable(musicBox.getAudioCable(), new HashSet<>());
        return null;
    }

    public static MusicController getMusicControllerByAudioCable(AudioCable audioCable) {
        return getControllerByAudioCable(audioCable, new HashSet<>());
    }

    public Set<MusicBox> getMusicBoxesByColor(int color) {
        return getMusicBoxesByColor(pos, color, new ArrayList<>());
    }

    private static Set<MusicBox> getMusicBoxesByColor(BlockPos blockPos, int color, List<BlockPos> checkedPositions) {
        Set<MusicBox> musicBoxes = new HashSet<>();

        List<AudioCable> audioCables = AudioCableListener.getAudioCablesByPos(blockPos);

        for (AudioCable audioCable : audioCables) {
            if (audioCable.getColor() == color || color == -1) {
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

                musicBoxes.addAll(getMusicBoxesByColor(newBlockPos, color, checkedPositions));
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

    public void update(MusicController controller) {
        MusicQueue newQueue = controller.getMusicQueue();
        this.musicQueue.update(newQueue);
        // TODO update tracks via sasving in file.
    }

    public void update(Map<Integer, Boolean> colors) {
        // TODO update via saving to file.
    }

    @Deprecated
    public void play(List<ServerPlayer> players, MusicBoxEvent clientMusicBoxManager) {
        players.forEach(player -> ServerMusicBoxListener.sendToClient(player, clientMusicBoxManager));
    }

    public MusicQueue getMusicQueue() {
        return this.musicQueue;
    }

    @Override
    public String toString() {
        return pos.toShortString().replaceAll(", ", ",") + "/" + musicQueue;
    }

    public static MusicController fromString(String data) {
        String[] parts = Utils.split(data, "/");
        return new MusicController(Utils.blockPosOf(parts[0]),
                MusicQueue.fromString(String.join("/", Arrays.copyOfRange(parts, 1, parts.length))));
    }
}
