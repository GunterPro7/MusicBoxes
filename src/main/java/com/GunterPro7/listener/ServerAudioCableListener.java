package com.GunterPro7.listener;

import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.entity.MusicBox;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerAudioCableListener extends AudioCableListener {
    @SubscribeEvent
    public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        Set<DyeColor> colors = new HashSet<>();
        getAudioCablesByPos(event.getPos()).forEach(cable -> colors.add(cable.getColor()));

        List<MusicBox> musicBoxesToDelete = ServerMusicBoxListener.getMusicBoxesContainingController(colors.stream().toList(), true);
        musicBoxesToDelete.forEach(MusicBox::powerDisconnected);

        List<BlockPos> posList = musicBoxesToDelete.stream().map(MusicBox::getBlockPos).toList();
        if (posList.size() != 0) {
            List<Float> volumeList = musicBoxesToDelete.stream().map(musicBox -> (float) musicBox.getVolume()).toList();

            MinecraftServer server = event.getLevel().getServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach(player ->
                        ServerMusicBoxListener.sendToClient(player, new MusicBoxEvent(false, null, posList, volumeList)));
            }
        }
    }
}
