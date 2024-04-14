package com.GunterPro7.block;

import com.GunterPro7.entity.MusicController;
import com.GunterPro7.entity.MusicQueue;
import com.GunterPro7.entity.MusicTrack;
import com.GunterPro7.utils.Utils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class MusicControllerBlockEntity extends BlockEntity {
    private final List<Integer> activeColors = new ArrayList<>();
    private final List<Integer> inactiveColors = new ArrayList<>();

    private List<MusicTrack> tracks = new ArrayList<>();
    private MusicQueue.PlayType playType;
    private int curPlayIndex;
    private boolean running;

    public MusicControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MUSIC_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        activeColors.clear();
        inactiveColors.clear();

        if (!tag.contains("running")) {
            return;
        }
        int[] arr = tag.getIntArray("active_colors");
        for (int i : arr) {
            activeColors.add(i);
        }
        arr = tag.getIntArray("inactive_colors");
        for (int i : arr) {
            inactiveColors.add(i);
        }

        String[] trackNames = tag.getString("tracks").split(";");
        String[] isMc = tag.getString("tracks_mc").split(";");
        String[] trackSize = tag.getString("tracks_length").split(";");

        tracks = new ArrayList<>();
        for (int i = 0; i < trackNames.length; i++) {
            tracks.add(new MusicTrack(trackNames[i], Boolean.parseBoolean(isMc[i]), Integer.parseInt(trackSize[i])));
        }


        playType = MusicQueue.PlayType.valueOf(tag.getByte("playType"));
        curPlayIndex = tag.getInt("playIndex");
        running = tag.getBoolean("running");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray("active_colors", activeColors);
        tag.putIntArray("inactive_colors", inactiveColors);

        tag.putString("tracks", Utils.listToShortString(tracks.stream().map(MusicTrack::getName).toList()));
        tag.putString("tracks_mc", Utils.listToShortString(tracks.stream().map(MusicTrack::isCustomSound).toList()));
        tag.putString("tracks_length", Utils.listToShortString(tracks.stream().map(MusicTrack::getLengthInSec).toList()));
        tag.putInt("playIndex", curPlayIndex);
        tag.putByte("playType", playType != null ? (byte) playType.getId() : 0);
        tag.putBoolean("running", running);
    }

    public Map<Integer, Boolean> getColorInfos() {
        Map<Integer, Boolean> colorInfos = new HashMap<>();
        activeColors.forEach(color -> colorInfos.put(color, true));
        inactiveColors.forEach(color -> colorInfos.put(color, false));

        return colorInfos;
    }

    public void update(Map<Integer, Boolean> colorInfos) {
        activeColors.clear();
        inactiveColors.clear();

        colorInfos.forEach((color, active) -> {
            if (active) {
                activeColors.add(color);
            } else {
                inactiveColors.add(color);
            }
        });

        setChanged();
    }

    public void update(MusicQueue musicQueue) {
        tracks = musicQueue.tracks();
        curPlayIndex = musicQueue.getIndex();
        playType = musicQueue.getPlayType();
        running = musicQueue.isRunning();

        setChanged();
    }

    public MusicQueue getNewQueue(MusicController controller) {
        return new MusicQueue(controller, playType == null ? MusicQueue.PlayType.REPEAT : playType, curPlayIndex, new ArrayList<>(tracks), running);
    }

    public void addColors(Set<Integer> colorsConnected) {
        for (int color : colorsConnected) {
            if (!inactiveColors.contains(color) && !activeColors.contains(color)) {
                inactiveColors.add(color);
            }
        }
    }
}
