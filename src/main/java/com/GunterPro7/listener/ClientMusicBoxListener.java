package com.GunterPro7.listener;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.ui.MusicBoxScreen;
import com.GunterPro7.utils.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientMusicBoxListener {
    @SubscribeEvent
    public void onRightClickMusicBox(BlockEvent.BreakEvent event) { // TODO check if BlockEvent.BreakEvent event is client sid
        if (McUtils.gameLoaded()) {
            if (event.getState().is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
                Minecraft.getInstance().doRunTask(() -> Minecraft.getInstance().setScreen(new MusicBoxScreen(new MusicBox(event.getPos(), true))));
            }
        }
    }
}
