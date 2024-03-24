package com.GunterPro7.listener;

import com.GunterPro7.Main;
import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.ui.MusicBoxScreen;
import com.GunterPro7.utils.ClientUtils;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.Utils;
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
        if (ClientUtils.gameLoaded() && !(Minecraft.getInstance().screen instanceof MusicBoxScreen)) {
            if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
                BlockPos pos = event.getPos();
                long id = Utils.getRandomId();
                MiscNetworkEvent.sendToServer(id, MiscAction.MUSIC_BOX_GET, pos.getX() + "," + pos.getY() + "," + pos.getZ());
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MusicBoxScreen(pos, id)));
            }
        }
    }

    @SubscribeEvent
    public void clientReceiveEvent(MiscNetworkEvent.ClientReceivedEvent event) {
        if (Minecraft.getInstance().screen instanceof MusicBoxScreen screen) {
            if (event.getAction() == MiscAction.MUSIC_BOX_GET && screen.id == event.getId()) {
                String[] data = event.getData().split("/");
                float volume = Float.parseFloat(data[0]);
                boolean active = Boolean.parseBoolean(data[1]);

                screen.updateInformation(volume, active);
            }
        }
    }
}
