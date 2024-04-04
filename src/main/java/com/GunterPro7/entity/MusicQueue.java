package com.GunterPro7.entity;

import com.GunterPro7.block.MusicControllerBlockEntity;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MusicQueue {
    private MusicController controller;
    private int curTrackIndex;
    private final List<String> tracks;
    private PlayType playType;
    private boolean running;

    public MusicQueue(MusicController controller) {
        this(controller, PlayType.REPEAT, 0, new ArrayList<>(), false);
    }

    public MusicQueue(MusicController controller, PlayType playType, int curTrackIndex, List<String> tracks, boolean running) {
        this.controller = controller;
        this.playType = playType;
        this.curTrackIndex = curTrackIndex;
        this.tracks = tracks;
        this.running = running;
    }

    public String getCurrentTrack() {
        return curTrackIndex < tracks.size() && curTrackIndex >= 0 ? tracks.get(curTrackIndex) : null;
    }

    public void play() {
        if (curTrackIndex < tracks.size()) {
            this.play(tracks.get(curTrackIndex));
        }
    }

    public void play(String track) {
        curTrackIndex = tracks.indexOf(track);
        controller.play(track, controller.getActiveColors());
    }

    public void pause() {
        controller.stop(controller.getActiveColors());
    }

    public void add(String track) {
        this.tracks.add(track);
    }

    public void remove(String track) {
        this.tracks.remove(track);
    }

    public List<String> tracks() {
        return tracks;
    }

    public void setPlayType(PlayType playType) {
        this.playType = playType;
    }

    public void update(MusicController controller, MusicQueue newQueue) {
        playType = newQueue.playType;
        tracks.clear();
        tracks.addAll(newQueue.tracks);
        curTrackIndex = newQueue.curTrackIndex;
        this.controller = newQueue.controller;

        MusicControllerBlockEntity blockEntity = (MusicControllerBlockEntity) controller.getLevel().getBlockEntity(controller.getPos());
        if (blockEntity != null) {
            blockEntity.update(this);
        }
    }

    public PlayType getPlayType() {
        return playType;
    }

    public int getIndex() {
        return this.curTrackIndex;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean switchRunning() {
        return running = !running;
    }

    public enum PlayType {
        REPEAT(0),
        RANDOM(1),
        REPEAT_TRACK(2),
        ;

        private final int id;
        PlayType(int id) {
            this.id = id;
        }

        public static PlayType valueOf(int id) {
            for (PlayType playType : values()) {
                if (playType.id == id) {
                    return playType;
                }
            }
            return null;
        }

        public int getId() {
            return this.id;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(playType.id + "/" + curTrackIndex + "/");
        tracks.forEach(track -> sb.append(track).append(";"));

        return sb.toString();
    }

    public static MusicQueue fromString(MusicController controller, String data) {
        String[] parts = Utils.split(data, "/");

        if (parts.length < 2) {
            return new MusicQueue(controller);
        }

        return new MusicQueue(controller, PlayType.valueOf(Integer.parseInt(parts[0])), Integer.parseInt(parts[1]),
                new ArrayList<>(Arrays.asList(parts[2].split(";"))), Boolean.parseBoolean(parts[3]));
    }
}
