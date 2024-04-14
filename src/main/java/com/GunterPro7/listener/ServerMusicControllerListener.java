package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.Main;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.entity.MusicTrack;
import com.GunterPro7.utils.ClientUtils;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.SoundUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

public class ServerMusicControllerListener {

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().is(ModBlocks.MUSIC_CONTROLLER_BLOCK.get())) {
            if (event.getEntity() == null) {
                event.setCanceled(true);
                return;
            }
            MusicController.musicControllers.add(new MusicController(event.getEntity().level(), event.getPos()));
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) throws IOException {
        if (event.getState().is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
            BlockPos pos = event.getPos();

            MusicController.musicControllers.remove(new MusicController(event.getPlayer().level(), pos));

            MusicController musicControllerToDelete = null;
            for (MusicController musicBox : MusicController.musicControllers) {
                if (pos.equals(musicBox.getPos())) {
                    musicControllerToDelete = musicBox;
                }
            }

            if (musicControllerToDelete != null) {
                MusicController.musicControllers.remove(musicControllerToDelete);
            }
        }
    }

    @SubscribeEvent
    public void serverReceiveEvent(MiscNetworkEvent.ServerReceivedEvent event) {
        MiscAction action = event.getAction();
        if (action == MiscAction.MUSIC_CONTROLLER_GET) {
            String[] data = event.getData().split("/");
            BlockPos pos = Utils.blockPosOf(data[0]);
            MusicController controller = MusicController.getController(event.getPlayer().level(), pos);

            if (controller != null) {
                StringBuilder sb = new StringBuilder();
                controller.getColorInfos().forEach((color, enabled) -> sb.append(enabled ? '1' : '0').append(color).append(";"));

                event.reply(controller + "/" + sb + "/" + controller.getMusicQueue().isRunning());
            }
        } else if (action == MiscAction.MUSIC_CONTROLLER_INNER_UPDATE) {
            String[] data = event.getData().split("/");
            BlockPos pos = Utils.blockPosOf(data[0]);
            MusicController controller = MusicController.getController(event.getPlayer().level(), pos);

            if (controller != null) {
                MusicController destController = MusicController.fromString(event.getPlayer().level(), event.getData()).clone();
                controller.update(destController);

                if (data.length > 4) {
                    controller.update(event.getPlayer().level(), Utils.integerBooleanListOf(data[4]));
                }
            }
        } else if (action == MiscAction.MUSIC_CONTROLLER_PLAY || action == MiscAction.MUSIC_CONTROLLER_STOP) {
            String[] data = Utils.split(event.getData(), "/");
            MusicController controller = MusicController.getController(event.getPlayer().level(), Utils.blockPosOf(data[0]));

            if (controller != null) {
                controller.getMusicQueue().newSongIntId();

                sendMusicRequestToClient(controller, data.length > 1 ? MusicTrack.fromString(data[1]) : null, action == MiscAction.MUSIC_CONTROLLER_PLAY);
            }
        }
    }

    public static void sendMusicRequestToClient(MusicController controller, MusicTrack musicTrack, boolean play) {
        if (controller != null && !(play && (musicTrack == null || musicTrack.getLocation() == null))) {
            List<MusicBox> musicBoxes = controller.getMusicBoxesByColor(controller.getActiveColors()).stream().filter(MusicBox::isActive).toList();
            List<BlockPos> posList = musicBoxes.stream().map(MusicBox::getBlockPos).toList();
            List<Float> volumeList = musicBoxes.stream().map(MusicBox::getVolume).toList();

            ResourceLocation location = musicTrack != null ? musicTrack.getLocation() : null;

            if (play) {
                controller.getMusicQueue().setLengthUntilAutoUpdate(musicTrack.getLengthInTicks() + 25);
            }

            MusicBoxEvent musicBoxEvent = new MusicBoxEvent(play, location, posList, volumeList, true);

            if (controller.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.players().forEach(player -> ServerMusicBoxListener.sendToClient(player, musicBoxEvent));
            }
        }
    }
}
