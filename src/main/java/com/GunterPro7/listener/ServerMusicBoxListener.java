package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerMusicBoxListener {
    private static final Random random = new Random();

    public static final List<MusicBox> musicBoxes = new ArrayList<>();

    public ServerMusicBoxListener() {

    }

    public static MusicBox getMusicBoxByPos(BlockPos blockPos) {
        for (MusicBox musicBox : musicBoxes) {
            if (musicBox.getBlockPos().equals(blockPos)) {
                return musicBox;
            }
        }
        return null;
    }

    public static boolean containsBlockPos(BlockPos pos) {
        for (MusicBox musicBox : musicBoxes) {
            if (musicBox.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().is(ModBlocks.MUSIC_BOX_BLOCK.get()) && !containsBlockPos(event.getPos())) {
            MusicBox musicBox = new MusicBox(event.getPos());

            FileManager.Positions.add(musicBox);
            musicBoxes.add(musicBox);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) throws IOException {
        if (event.getState().is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
            BlockPos pos = event.getPos();

            MusicBox musicBox = new MusicBox(pos);

            FileManager.Positions.remove(musicBox);
            musicBoxes.remove(musicBox);

            MusicBox musicBoxToDelete = null;
            for (MusicBox curMusicBox : musicBoxes) {
                if (pos.equals(curMusicBox.getBlockPos())) {
                    musicBoxToDelete = curMusicBox;
                }
            }

            if (musicBoxToDelete != null) {
                musicBoxes.remove(musicBoxToDelete);
            }
        }
    }

    @SubscribeEvent
    public void onServerReceive(MiscNetworkEvent.ServerReceivedEvent event) throws IOException {
        String[] data = event.getData().split("/");
        MiscAction action = event.getAction();

        if (action == MiscAction.MUSIC_BOX_GET) {
            if (data.length > 0) {
                MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(Utils.blockPosOf(data[0]));

                if (musicBox != null) {
                    MiscNetworkEvent.sendToClient(event.getPlayer(), event.getId(), MiscAction.MUSIC_BOX_GET, musicBox.getVolume() + "/" + musicBox.isActive());
                }
            }
        } else if (action == MiscAction.MUSIC_BOX_INNER_UPDATE) {
            if (data.length > 2) {
                MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(Utils.blockPosOf(data[0]));

                if (musicBox != null) {
                    musicBox.setVolume(Float.parseFloat(data[1]));
                    musicBox.setActive(Boolean.parseBoolean(data[2]));
                    FileManager.Positions.update(musicBox);
                }

            }
        }
    }


    public static List<MusicBox> getMusicBoxesContainingController(List<DyeColor> colors, boolean invert) {
        List<MusicBox> musicBoxes = new ArrayList<>();
        for (MusicBox musicBox : ServerMusicBoxListener.musicBoxes) {
            if (musicBox.isPowered() && colors.contains(musicBox.getAudioCable().getColor())) { // or implement also for "all"
                MusicController musicController = MusicController.getMusicControllerByMusicBox(musicBox);
                if (invert == (musicController == null)) {
                    musicBoxes.add(musicBox);
                }
            }
        }

        return musicBoxes;
    }

    public static void sendToClient(ServerPlayer player, MusicBoxEvent message) {
        MusicBoxEvent.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    @Deprecated
    private void broadcastSound(PlayerList players, Level level, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
        net.minecraftforge.event.PlayLevelSoundEvent.AtPosition event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtPosition(level, x, y, z, sound, source, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        source = event.getSource();
        volume = event.getNewVolume();
        pitch = event.getNewPitch();

        players.broadcast(null, x, y, z, Double.MAX_VALUE, level.dimension(), new ClientboundSoundPacket(sound, source, x, y, z, volume, pitch, random.nextLong()));
    }
}
