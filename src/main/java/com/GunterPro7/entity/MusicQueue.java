package com.GunterPro7.entity;

import java.util.ArrayList;
import java.util.List;

public class MusicQueue {
    private int curTrackIndex;
    private final List<String> tracks = new ArrayList<>();
    private PlayType playType;


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


    public enum PlayType {
        REPEAT,
        RANDOM,
        REPEAT_TRACK,
    }
}
