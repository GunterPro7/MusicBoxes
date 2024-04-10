package com.GunterPro7.entity;

import com.GunterPro7.block.MusicControllerBlockEntity;
import com.GunterPro7.listener.ServerMusicControllerListener;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.TimeUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MusicQueue {
    private static final Random random = new Random();

    private MusicController controller;
    private int curTrackIndex;
    private final List<String> tracks;
    private PlayType playType;
    private boolean running;

    private long timeId;

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

    public ResourceLocation getLocationOfCurrentTrack() {
        String curTrack = getCurrentTrack();

        if (curTrack != null) {
            String mcLocation = "minecraft:music_disc." + curTrack;
            if (McUtils.MUSIC_DISCS.containsKey(mcLocation)) {
                return new ResourceLocation(mcLocation);
            } else {
                return new ResourceLocation("musicboxes", "sounds/" + curTrack);
            }
        }

        return null;
    }

    public void play() {
        if (curTrackIndex < tracks.size()) {
            this.play(tracks.get(curTrackIndex));
        }
    }

    public void play(String track) {
        curTrackIndex = tracks.indexOf(track);
        controller.play(track);
    }

    public void pause() {
        controller.stop();
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
        running = newQueue.running;

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

    public void setLengthUntilAutoUpdate(int ticks) {
        long id = Utils.getRandomId();
        this.timeId = id;

        TimeUtils.addJob(ticks * 50, () -> {
            if (id == timeId && isRunning()) {
                nextSong();
            }
        });
    }

    private void nextSong() {
        switch (playType) {
            case REPEAT -> curTrackIndex = curTrackIndex >= tracks.size() ? 0 : curTrackIndex + 1;
            case RANDOM -> curTrackIndex = random.nextInt(tracks.size());
        }

        ServerMusicControllerListener.sendMusicRequestToClient(controller, getLocationOfCurrentTrack(), true, true);
    }

    public enum PlayType {
        REPEAT(0),
        RANDOM(1),
        REPEAT_TRACK(2),
        ;

        private static final PlayType DEFAULT = PlayType.REPEAT;
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
            return PlayType.DEFAULT;
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
                new ArrayList<>(Arrays.asList(parts[2].split(";"))), Boolean.parseBoolean(parts[4]));
    }
}
