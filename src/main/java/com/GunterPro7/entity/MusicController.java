package com.GunterPro7.entity;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.block.MusicControllerBlockEntity;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.listener.AudioCableListener;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.listener.ServerMusicBoxListener;
import com.GunterPro7.ui.MusicControllerScreen;
import com.GunterPro7.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class MusicController {
    public static final List<MusicController> musicControllers = new ArrayList<>();
    private final BlockPos pos;
    private final MusicQueue musicQueue;
    private final Level level;

    public MusicController(Level level, BlockPos pos) {
        this(level, pos, null);
    }

    public MusicController(Level level, BlockPos pos, MusicQueue queue) {
        this.level = level;
        this.pos = pos;
        if (queue == null) {
            queue = loadQueue(this, level, pos);
        }
        this.musicQueue = queue;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Level getLevel() {
        return level;
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
        MusicControllerBlockEntity blockEntity = (MusicControllerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity != null) {
            Map<Integer, Boolean> colorInfos = blockEntity.getColorInfos();
            Set<Integer> colorsConnected = getColorsConnected();
            if (colorInfos.size() != colorsConnected.size()) {
                blockEntity.addColors(colorsConnected);
                colorInfos = blockEntity.getColorInfos();
            }
            return colorInfos;
        } else {
            level.setBlockEntity(new MusicControllerBlockEntity(pos, level.getBlockState(pos)));
            return new HashMap<>();
        }
    }

    public List<Integer> getActiveColors() {
        List<Integer> activeColors = new ArrayList<>();

        getColorInfos().forEach((color, info) -> {
            if (info) {
                activeColors.add(color);
            }
        });

        return activeColors;
    }

    public static MusicController getMusicControllerByMusicBox(Level level, MusicBox musicBox) {
        if (musicBox.hasAudioCable()) return getControllerByAudioCable(level, musicBox.getAudioCable(), new HashSet<>());
        return null;
    }

    public static MusicController getMusicControllerByAudioCable(Level level, AudioCable audioCable) {
        return getControllerByAudioCable(level, audioCable, new HashSet<>());
    }

    public Set<MusicBox> getMusicBoxesByColor(int color) {
        return getMusicBoxesByColor(pos, List.of(color), new ArrayList<>());
    }

    public Set<MusicBox> getMusicBoxesByColor(List<Integer> colors) {
        if (colors.contains(-1)) {
            colors = List.of(-1);
        }
        return getMusicBoxesByColor(pos, colors, new ArrayList<>());
    }

    private static Set<MusicBox> getMusicBoxesByColor(BlockPos blockPos, List<Integer> colors, List<BlockPos> checkedPositions) {
        Set<MusicBox> musicBoxes = new HashSet<>();

        List<AudioCable> audioCables = AudioCableListener.getAudioCablesByPos(blockPos);

        for (AudioCable audioCable : audioCables) {
            if (colors.contains(audioCable.getColor()) || colors.contains(-1)) {
                if (audioCable.getMusicBoxStart() != null) {
                    musicBoxes.add(audioCable.getMusicBoxStart());
                } if (audioCable.getMusicBoxEnd() != null) {
                    musicBoxes.add(audioCable.getMusicBoxEnd());
                }

                BlockPos newBlockPos = null;
                if (!checkedPositions.contains(audioCable.getStartBlock())) {
                    newBlockPos = audioCable.getStartBlock() ;
                } else {
                    if (!checkedPositions.contains(audioCable.getEndBlock())) {
                        newBlockPos = audioCable.getEndBlock();
                    }
                }

                checkedPositions.add(blockPos);
                if (newBlockPos != null) {
                    musicBoxes.addAll(getMusicBoxesByColor(newBlockPos, colors, checkedPositions));
                }
            }
        }

        return musicBoxes;
    }

    private static MusicController getControllerByAudioCable(Level level, AudioCable audioCable, Set<BlockPos> checkedPositions) {
        BlockPos startBlock = audioCable.getStartBlock();
        BlockPos endBlock = audioCable.getEndBlock();

        MusicController musicControllerStart = MusicController.getController(level, startBlock);
        if (musicControllerStart != null) {
            return musicControllerStart;
        }

        MusicController musicControllerEnd = MusicController.getController(level, endBlock);
        if (musicControllerEnd != null) {
            return musicControllerEnd;
        }

        checkedPositions.add(startBlock);
        checkedPositions.add(endBlock);

        List<AudioCable> startBlockCables = AudioCableListener.getAudioCablesByPos(startBlock);
        for (AudioCable cable : startBlockCables) {
            BlockPos pos = cable.getStartBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getControllerByAudioCable(level, cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        List<AudioCable> endBlockCables = AudioCableListener.getAudioCablesByPos(endBlock);
        for (AudioCable cable : endBlockCables) {
            BlockPos pos = cable.getEndBlock();
            if (!checkedPositions.contains(pos)) {
                MusicController controllerPos = getControllerByAudioCable(level, cable, checkedPositions);
                if (controllerPos != null) {
                    return controllerPos;
                }
            }
        }

        return null;
    }

    public static MusicController getController(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.MUSIC_CONTROLLER_BLOCK.get()) ? new MusicController(level, pos): null;
    }

    public void update(MusicController controller) {
        MusicQueue newQueue = controller.getMusicQueue();
        this.musicQueue.update(this, newQueue);
    }

    public void update(Level level, Map<Integer, Boolean> colors) {
        MusicControllerBlockEntity blockEntity = (MusicControllerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null) {
            blockEntity = new MusicControllerBlockEntity(pos, level.getBlockState(pos));
            level.setBlockEntity(blockEntity);
        }

        blockEntity.update(colors);
    }

    private static MusicQueue loadQueue(MusicController controller, Level level, BlockPos pos) {
        MusicControllerBlockEntity blockEntity = (MusicControllerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity != null) {
            return blockEntity.getNewQueue(controller);
        }

        return null;
    }

    @Deprecated
    public void play(List<ServerPlayer> players, MusicBoxEvent clientMusicBoxManager) {
        players.forEach(player -> ServerMusicBoxListener.sendToClient(player, clientMusicBoxManager));
    }

    @OnlyIn(Dist.CLIENT)
    protected void play(String track) {
        MiscNetworkEvent.sendToServer(new MiscNetworkEvent(-1, MiscAction.MUSIC_CONTROLLER_PLAY, pos.toShortString() + "/" + track));
    }

    @OnlyIn(Dist.CLIENT)
    protected void stop() {
        MiscNetworkEvent.sendToServer(new MiscNetworkEvent(-1, MiscAction.MUSIC_CONTROLLER_STOP, pos.toShortString()));
    }

    public MusicQueue getMusicQueue() {
        return this.musicQueue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicController that = (MusicController) o;
        return Objects.equals(pos, that.pos) && Objects.equals(level, that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, level);
    }

    @Override
    public String toString() {
        return pos.toShortString().replaceAll(", ", ",") + "/" + musicQueue;
    }

    public static MusicController fromString(Level level, String data) {
        String[] parts = Utils.split(data, "/");

        MusicController controller = new MusicController(level, Utils.blockPosOf(parts[0]));
        controller.getMusicQueue().update(controller, MusicQueue.fromString(controller, String.join("/", Arrays.copyOfRange(parts, 1, parts.length))));

        return controller;
    }
}
