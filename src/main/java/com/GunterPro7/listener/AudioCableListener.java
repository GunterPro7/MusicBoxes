package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.Main;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.utils.ClientUtils;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.*;

// Client & Server Side
public class AudioCableListener {
    public static final List<AudioCable> audioCables = new ArrayList<>();
    private static final List<Level> loadedLevels = new ArrayList<>();
    public static final Map<LivingEntity, List<AudioCable>> FETCHED_CABLE_MAP = new HashMap<>();

    public static List<AudioCable> getAudioCablesByPos(BlockPos pos) {
        List<AudioCable> newAudioCables = new ArrayList<>();
        for (AudioCable audioCable : audioCables) {
            if (pos.equals(audioCable.getStartBlock()) || pos.equals(audioCable.getEndBlock())) {
                newAudioCables.add(audioCable);
            }
        }
        return newAudioCables;
    }

    @SubscribeEvent
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) throws IOException { // TODO is this call client side?
        BlockPos pos = event.getPos();
        List<AudioCable> audioCableList = new ArrayList<>();

        audioCables.forEach(audioCable -> {
            BlockPos startBlock = audioCable.getStartBlock();
            BlockPos endBlock = audioCable.getEndBlock();

            int posHashCode = pos.hashCode();
            if ((startBlock.hashCode() == posHashCode && startBlock.equals(pos)) || (endBlock.hashCode() == posHashCode && endBlock.equals(pos))) {
                if (audioCable.getMusicBoxStart() != null) audioCable.getMusicBoxStart().powerDisconnected();
                if (audioCable.getMusicBoxEnd() != null) audioCable.getMusicBoxEnd().powerDisconnected();
                audioCableList.add(audioCable);
                if (Main.serverSide || ClientUtils.isSinglePlayer()) {
                    audioCable.drop();
                }
            }
        });

        boolean audioCablesRemoved = false;

        if (Main.serverSide || ClientUtils.isSinglePlayer() && audioCableList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (AudioCable audioCable : audioCableList) {
                sb.append(audioCable.toString()).append('/');
            }

            event.getLevel().players().forEach(player -> MiscNetworkEvent.sendToClient((ServerPlayer) player, new MiscNetworkEvent(-1, MiscAction.AUDIO_CABLE_REMOVE, sb.toString())));
            FileManager.AudioCables.removeAll(audioCableList);

            MusicController musicController = MusicController.getMusicControllerByMusicBox(event.getPlayer().level(), new MusicBox(event.getPos(), event.getPlayer().level()));

            if (musicController != null) {
                Set<MusicBox> musicBoxesBefore = musicController.getMusicBoxesByColor(-1); // TODO How should I do that with the colors?

                audioCables.removeAll(audioCableList);
                audioCablesRemoved = true;

                Set<MusicBox> musicBoxesAfter = musicController.getMusicBoxesByColor(-1);
                List<MusicBox> musicBoxesToDelete = musicBoxesBefore.stream().filter(musicBox -> !musicBoxesAfter.contains(musicBox) && musicBox.isActive()).toList();

                List<BlockPos> posList = musicBoxesToDelete.stream().map(MusicBox::getBlockPos).toList();
                if (posList.size() != 0) {
                    List<Float> volumeList = musicBoxesToDelete.stream().map(MusicBox::getVolume).toList();
                    event.getLevel().players().forEach(player -> ServerMusicBoxListener.sendToClient((ServerPlayer) player, new MusicBoxEvent(false, null, posList, volumeList)));
                }
            }
        }

        if (!audioCablesRemoved) {
            audioCables.removeAll(audioCableList);
        }
    }

    @SubscribeEvent
    public void onAudioCablePost(MiscNetworkEvent.ServerReceivedEvent event) throws IOException {
        MiscAction action = event.getAction();
        String[] data = event.getData().split("/");
        Level level = event.getPlayer().level();

        if (action == MiscAction.AUDIO_CABLE_IS_FREE) {
            BlockPos pos = Utils.blockPosOf(data[0]);

            if (ServerMusicBoxListener.containsBlockPos(level, pos)) {
                MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(level, pos);

                event.reply(String.valueOf(!(musicBox != null && musicBox.isPowered())));
            } else {
                event.reply("true");
            }

        } else if (action == MiscAction.AUDIO_CABLE_POST) {
            List<String> parts = Arrays.stream(data[0].split(";")).map(s -> s.split(":")[1]).toList();
            AudioCable audioCable = new AudioCable(Utils.vec3Of(parts.get(0)), Utils.vec3Of(parts.get(1)),
                    Utils.blockPosOf(parts.get(2)), Utils.blockPosOf(parts.get(3)), event.getPlayer().level(), Integer.parseInt(parts.get(5)));

            if (audioCable.getBlockDistance() <= 32d && !ServerMusicBoxListener.isPowered(level, audioCable.getStartBlock())
                    && !ServerMusicBoxListener.isPowered(level, audioCable.getEndBlock())) {
                audioCables.add(audioCable);
                FileManager.AudioCables.add(audioCable);

                MusicBox musicBox = audioCable.getMusicBoxEnd();
                if (musicBox != null) {
                    musicBox.powerConnected(audioCable);
                }

                musicBox = audioCable.getMusicBoxStart();
                if (musicBox != null) {
                    musicBox.powerConnected(audioCable);
                }

                event.getPlayer().level().players().forEach(player -> {
                    if (player != event.getPlayer()) {
                        MiscNetworkEvent.sendToClient((ServerPlayer) player,
                                new MiscNetworkEvent(-1, MiscAction.AUDIO_CABLE_NEW, audioCable.toString()));
                    }
                });
            } else {
                MiscNetworkEvent.sendToClient(event.getPlayer(), -1, MiscAction.AUDIO_CABLE_REMOVE, event.getData());
            }

        }
    }

    @SubscribeEvent
    public void onLevelChange(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                if (!loadedLevels.contains(level)) {
                    audioCables.addAll(FileManager.AudioCables.getAll(level));
                    loadedLevels.add(level);
                }

                List<AudioCable> newAudioCables = audioCables.stream().filter(audioCable -> audioCable.getLevel() == level).toList();

                if (newAudioCables.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (AudioCable audioCable : newAudioCables) {
                        sb.append(audioCable.toString()).append('/');
                    }

                    MiscNetworkEvent.sendToClient(serverPlayer, -1, MiscAction.AUDIO_CABLE_FETCH, sb.toString());
                }
            }
        }

    }

    //@SubscribeEvent
    //public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) throws IOException {
    //    if (System.currentTimeMillis() - timePos1 < 250) return;
//
    //    Vec3 newPos = event.getHitVec().getLocation();
//
    //    DyeColor dyeColor = DyeColor.getColor(event.getItemStack());
    //    if (dyeColor == null) return;
//
    //    MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(event.getPos());
    //    if (musicBox == null) musicBox = ServerMusicBoxListener.getMusicBoxByPos(block1);
    //    if (musicBox != null) {
    //        if (musicBox.isPowered()) {
    //            McUtils.sendPrivateChatMessage("This music box already has a Audio Cable connected!");
    //            return;
    //        }
    //    }
//
    //    if (pos1 != null && !pos1.equals(newPos)) {
    //        AudioCable audioCable = new AudioCable(pos1, newPos, block1, event.getPos(), dyeColor);
    //        if (audioCable.getBlockDistance() > 32d) {
    //            McUtils.sendPrivateChatMessage("The Audio-wire cant be longer then 32 blocks!");
    //        } else {
    //            audioCables.add(audioCable);
    //            if (Main.serverSide || McUtils.isSinglePlayer()) {
    //                FileManager.AudioCables.add(audioCable);
    //            }
//
    //            pos1 = null;
    //            block1 = null;
    //            if (musicBox != null) {
    //                musicBox.powerConnected(audioCable);
    //            }
    //        }
    //    } else {
    //        pos1 = newPos;
    //        block1 = event.getPos();
    //    }
    //    timePos1 = System.currentTimeMillis();
    //}
}
