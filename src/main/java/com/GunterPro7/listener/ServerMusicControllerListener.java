package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;

public class ServerMusicControllerListener {
    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().is(ModBlocks.MUSIC_CONTROLLER_BLOCK.get())) {
            FileManager.Controller.add(event.getPos());
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

            FileManager.Controller.remove(pos);

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
                MusicController destController = MusicController.fromString(event.getPlayer().level(), event.getData());
                controller.update(destController);

                if (data.length > 4) {
                    controller.update(event.getPlayer().level(), Utils.integerBooleanListOf(data[4]));
                }
            }
        } else if (action == MiscAction.MUSIC_CONTROLLER_PLAY) {
            // TODO hier aufgehlrt
        } else if (action == MiscAction.MUSIC_CONTROLLER_STOP) {

        }
    }
}
