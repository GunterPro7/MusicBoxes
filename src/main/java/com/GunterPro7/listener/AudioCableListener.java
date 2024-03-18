package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.utils.McUtils;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Client & Server Side
public class AudioCableListener {
    public static final List<AudioCable> audioCables = new ArrayList<>();
    @Nullable
    protected Vec3 pos1;
    protected long timePos1;
    @Nullable
    protected BlockPos block1;

    protected static VertexBuffer vertexBuffer;

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
            FileManager.AudioCables.removeAll(audioCableList);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) throws IOException {
        if (System.currentTimeMillis() - timePos1 < 250) return;

        Vec3 newPos = event.getHitVec().getLocation();

        DyeColor dyeColor = DyeColor.getColor(event.getItemStack());
        if (dyeColor == null) return;

        MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(event.getPos());
        if (musicBox == null) musicBox = ServerMusicBoxListener.getMusicBoxByPos(block1);
        if (musicBox != null) {
            if (musicBox.isPowered()) {
                McUtils.sendPrivateChatMessage("This music box already has a Audio Cable connected!");
                return;
            }
        }

        if (pos1 != null && !pos1.equals(newPos)) {
            AudioCable audioCable = new AudioCable(pos1, newPos, block1, event.getPos(), dyeColor);
            if (audioCable.getBlockDistance() > 32d) {
                McUtils.sendPrivateChatMessage("The Audio-wire cant be longer then 32 blocks!");
            } else {
                audioCables.add(audioCable);
                if (McUtils.isServerSide() || McUtils.isSinglePlayer()) {
                    FileManager.AudioCables.add(audioCable);
                }

                pos1 = null;
                block1 = null;
                if (musicBox != null) {
                    musicBox.powerConnected(audioCable);
                }
            }
        } else {
            pos1 = newPos;
            block1 = event.getPos();
        }
        timePos1 = System.currentTimeMillis();
    }
}
