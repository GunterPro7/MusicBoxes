package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.*;

// Client & Server Side
public class AudioCableListener {
    public static final List<AudioCable> audioCables = new ArrayList<>();
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
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) throws IOException {
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
                audioCable.drop(event.getPlayer().level());
            }
        });


        audioCables.removeAll(audioCableList);

        if (McUtils.isServerSide() || McUtils.isSinglePlayer()) {
            FileManager.AudioCables.removeAll(audioCableList); // TODO zum client schicken

            Set<DyeColor> colors = new HashSet<>();
            getAudioCablesByPos(event.getPos()).forEach(cable -> colors.add(cable.getColor()));

            List<MusicBox> musicBoxesToDelete = ServerMusicBoxListener.getMusicBoxesContainingController(colors.stream().toList(), true);
            musicBoxesToDelete.forEach(MusicBox::powerDisconnected);

            List<BlockPos> posList = musicBoxesToDelete.stream().map(MusicBox::getBlockPos).toList();
            if (posList.size() != 0) {
                List<Float> volumeList = musicBoxesToDelete.stream().map(MusicBox::getVolume).toList();

                MinecraftServer server = event.getLevel().getServer();
                if (server != null) {
                    server.getPlayerList().getPlayers().forEach(player ->
                            ServerMusicBoxListener.sendToClient(player, new MusicBoxEvent(false, null, posList, volumeList)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onAudioCablePost(MiscNetworkEvent.ServerReceivedEvent event) throws IOException {
        MiscAction action = event.getAction();
        String[] data = event.getData().split("/");

        if (action == MiscAction.AUDIO_CABLE_IS_FREE) {
            BlockPos pos = Utils.blockPosOf(data[0]);

            if (ServerMusicBoxListener.containsBlockPos(pos)) {
                MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(pos);

                event.reply(String.valueOf(!(musicBox != null && musicBox.isPowered())));
            } else {
                event.reply("True");
            }

        } else if (action == MiscAction.AUDIO_CABLE_POST) {
            List<String> parts = Arrays.stream(data[0].split(";")).map(s -> s.split(":")[1]).toList();
            AudioCable audioCable = new AudioCable(Utils.vec3Of(parts.get(0)), Utils.vec3Of(parts.get(1)),
                    Utils.blockPosOf(parts.get(2)), Utils.blockPosOf(parts.get(3)), event.getPlayer().level(), DyeColor.valueOf(parts.get(5)));

            if (audioCable.getBlockDistance() <= 32d) {
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

                event.getPlayer().level().players().forEach(player -> MiscNetworkEvent.sendToClient((ServerPlayer) player,
                        new MiscNetworkEvent(-1, MiscAction.AUDIO_CABLE_FETCH, audioCable.toString())));
            }

        } else if (action == MiscAction.AUDIO_CABLE_FETCH) {
            BlockPos playerPos = Utils.blockPosOf(data[0]);

            //List<AudioCable> fetchedCables = FETCHED_CABLE_MAP.computeIfAbsent(event.getPlayer(), k -> new ArrayList<>());
            //List<AudioCable> cablesForClient = new ArrayList<>();

            List<AudioCable> newAudioCables = new ArrayList<>();
            for (AudioCable audioCable : audioCables) {
                if (true) {

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
    //            if (McUtils.isServerSide() || McUtils.isSinglePlayer()) {
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
