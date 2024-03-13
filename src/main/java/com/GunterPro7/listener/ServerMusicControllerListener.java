package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.entity.MusicController;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;

public class ServerMusicControllerListener {
    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().is(ModBlocks.MUSIC_CONTROLLER_BLOCK.get())) {
            FileManager.Controller.add(event.getPos());
            MusicController.musicControllers.add(new MusicController(event.getPos()));
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) throws IOException {
        if (event.getState().is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
            BlockPos pos = event.getPos();

            FileManager.Controller.remove(pos);
            MusicController.musicControllers.remove(new MusicController(pos));

            MusicController musicBoxToDelete = null;
            for (MusicController musicBox : MusicController.musicControllers) {
                if (pos.equals(musicBox.getPos())) {
                    musicBoxToDelete = musicBox;
                }
            }

            if (musicBoxToDelete != null) {
                MusicController.musicControllers.remove(musicBoxToDelete);
            }
        }
    }
}
