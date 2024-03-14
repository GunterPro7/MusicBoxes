package com.GunterPro7.listener;

import com.GunterPro7.Main;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.ui.MusicBoxScreen;
import com.GunterPro7.utils.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientMusicBoxListener {
    @SubscribeEvent
    public void onRightClickMusicBox(PlayerInteractEvent.RightClickBlock event) {
        if (McUtils.gameLoaded() && !(Minecraft.getInstance().screen instanceof MusicBoxScreen)) {
            if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
                BlockPos pos = event.getPos();
                long id = 1L + (long) (Math.random() * (Long.MAX_VALUE - 1L));
                MiscNetworkEvent.sendToServer("musicBox/get/" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "/", id);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MusicBoxScreen(new MusicBox(pos, 50d, true), id)));
            }
        }
    }

    @SubscribeEvent
    public void clientReceiveEvent(MiscNetworkEvent.ClientReceivedEvent event) {
        if (Minecraft.getInstance().screen instanceof MusicBoxScreen screen) {
            if (screen.id == event.getId()) {
                String[] data = event.getData().split("/");
                double volume = Double.parseDouble(data[0]);
                boolean active = Boolean.parseBoolean(data[1]);

                screen.updateInformation(volume, active);
            }
        }
    }
}
