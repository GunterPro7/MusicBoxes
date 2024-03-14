package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.block.ModBlocks;
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

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
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
    public void onServerReceive(MiscNetworkEvent.ServerReceivedEvent event) {
        String[] data = event.getData().split("/");

        if (data.length > 1) {
            if (data[0].equals("musicBox")) {
                if (data[1].equals("get")) {
                    if (data.length > 2) {
                        MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(Utils.blockPosOf(data[2]));

                        if (musicBox != null) {
                            MiscNetworkEvent.sendToClient(event.getPlayer(), musicBox.getVolume() + "/" + musicBox.isActive(), event.getId());
                        }
                    }
                } else if (data[1].equals("update")) {
                    if (data.length > 4) {
                        MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(Utils.blockPosOf(data[2]));

                        if (musicBox != null) {
                            musicBox.setVolume(Double.parseDouble(data[3]));
                            musicBox.setActive(Boolean.parseBoolean(data[4]));
                        }
                    }
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
