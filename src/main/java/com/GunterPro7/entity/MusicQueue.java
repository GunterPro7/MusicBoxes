package com.GunterPro7.entity;

import com.GunterPro7.block.MusicControllerBlockEntity;
import com.GunterPro7.listener.ServerMusicControllerListener;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.TimeUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class MusicQueue {
    private static final Random random = new Random();

    private MusicController controller;
    private int curTrackIndex;
    private final List<MusicTrack> tracks;
    private PlayType playType;
    private boolean running;

    private long timeId;
    private AtomicLong songIntId = new AtomicLong(0);

    public MusicQueue(MusicController controller) {
        this(controller, PlayType.REPEAT, 0, new ArrayList<>(), false);
    }

    public MusicQueue(MusicController controller, PlayType playType, int curTrackIndex, List<MusicTrack> tracks, boolean running) {
        this.controller = controller;
        this.playType = playType;
        this.curTrackIndex = curTrackIndex;
        this.tracks = tracks;
        this.running = running;
    }

    public MusicTrack getCurrentTrack() {
        return curTrackIndex < tracks.size() && curTrackIndex >= 0 ? tracks.get(curTrackIndex) : null;
    }

    @OnlyIn(Dist.CLIENT)
    public void play() {
        if (curTrackIndex < tracks.size()) {
            this.play(tracks.get(curTrackIndex));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void play(MusicTrack track) {
        curTrackIndex = tracks.indexOf(track);
        controller.play(track);
    }

    @OnlyIn(Dist.CLIENT)
    public void pause() {
        controller.stop();
    }

    public MusicTrack getTrackByName(String trackName) {
        for (MusicTrack track : tracks) {
            if (track.getName().equals(trackName)) {
                return track;
            }
        }
        return null;
    }

    public void add(MusicTrack track) {
        this.tracks.add(track);
    }

    public void remove(MusicTrack track) {
        this.tracks.remove(track);
    }

    public List<MusicTrack> tracks() {
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
        long curId = Utils.getRandomId();
        this.timeId = curId;

        AtomicLong interactionId = new AtomicLong(this.songIntId.get());

        TimeUtils.addJob(curId, ticks * 50, id -> {
            if (id == timeId && isRunning() && interactionId.get() == this.songIntId.get()) {
                nextSong();
            }
        });
    }

    public void newSongIntId() {
        this.songIntId.set(Utils.getRandomId());
    }

    private void nextSong() {
        switch (playType) {
            case REPEAT -> curTrackIndex = curTrackIndex + 1 >= tracks.size() ? 0 : curTrackIndex + 1;
            case RANDOM -> curTrackIndex = random.nextInt(tracks.size());
        }

        MusicTrack track = getCurrentTrack();

        setLengthUntilAutoUpdate(track.getLengthInTicks() + 25);
        ServerMusicControllerListener.sendMusicRequestToClient(controller, track, true);
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
        tracks.forEach(track -> sb.append(track).append("?"));

        return sb.toString();
    }

    public static MusicQueue fromString(MusicController controller, String data) {
        String[] parts = Utils.split(data, "/");

        if (parts.length < 2) {
            return new MusicQueue(controller);
        }

        return new MusicQueue(controller, PlayType.valueOf(Integer.parseInt(parts[0])), Integer.parseInt(parts[1]),
                parts[2].isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.stream(parts[2].split("\\?"))
                        .map(e -> MusicTrack.fromString(e)).toList()), Boolean.parseBoolean(parts[4])); // Dont Change e -> MusicTrack.fromString(e), it causes an error? SMH
    }
}
