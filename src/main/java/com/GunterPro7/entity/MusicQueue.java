package com.GunterPro7.entity;

import com.GunterPro7.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MusicQueue {
    private int curTrackIndex;
    private final List<String> tracks;
    private PlayType playType;

    public MusicQueue() {
        this(PlayType.REPEAT, 0, new ArrayList<>());
    }

    private MusicQueue(PlayType playType, int curTrackIndex, List<String> tracks) {
        this.playType = playType;
        this.curTrackIndex = curTrackIndex;
        this.tracks = tracks;
    }

    public String getCurrentTrack() {
        return curTrackIndex < tracks.size() && curTrackIndex >= 0 ? tracks.get(curTrackIndex) : null;
    }

    public void play(String track) {
        curTrackIndex = tracks.indexOf(track);
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

    public void update(MusicQueue newQueue) {
        playType = newQueue.playType;
        tracks.clear();
        tracks.addAll(newQueue.tracks);
        curTrackIndex = newQueue.curTrackIndex;
    }

    public PlayType getPlayType() {
        return playType;
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
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(playType.id + "/" + curTrackIndex + "/");
        tracks.forEach(track -> sb.append(track).append(";"));

        return sb.toString();
    }

    public static MusicQueue fromString(String data) {
        String[] parts = Utils.split(data, "/");

        if (parts.length < 2) {
            return new MusicQueue();
        }

        return new MusicQueue(PlayType.valueOf(Integer.parseInt(parts[0])), Integer.parseInt(parts[1]),
                new ArrayList<>(Arrays.asList(parts[2].split(";"))));
    }
}
