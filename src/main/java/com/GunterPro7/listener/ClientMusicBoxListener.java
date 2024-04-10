package com.GunterPro7.listener;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.ui.MusicBoxScreen;
import com.GunterPro7.ui.MusicControllerScreen;
import com.GunterPro7.utils.ClientUtils;
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
        if (ClientUtils.gameLoaded()) {
            BlockPos pos = event.getPos();

            if (!(Minecraft.getInstance().screen instanceof MusicBoxScreen)) {
                if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.MUSIC_BOX_BLOCK.get())) {
                    long id = Utils.getRandomId();
                    MiscNetworkEvent.sendToServer(id, MiscAction.MUSIC_BOX_GET, pos.getX() + "," + pos.getY() + "," + pos.getZ());
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MusicBoxScreen(pos, id)));
                }
            } if (!(Minecraft.getInstance().screen instanceof MusicControllerScreen)) {
                if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.MUSIC_CONTROLLER_BLOCK.get())) {
                    long id = Utils.getRandomId();
                    MiscNetworkEvent.sendToServer(id, MiscAction.MUSIC_CONTROLLER_GET, pos.getX() + "," + pos.getY() + "," + pos.getZ());
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MusicControllerScreen(id, null)));
                }
            }
        }
    }

    @SubscribeEvent
    public void clientReceiveEvent(MiscNetworkEvent.ClientReceivedEvent event) {
        MiscAction action = event.getAction();
        if (Minecraft.getInstance().screen instanceof MusicBoxScreen screen) {
            if (action == MiscAction.MUSIC_BOX_GET && screen.interactionId == event.getId()) {
                String[] data = event.getData().split("/");
                float volume = Float.parseFloat(data[0]);
                boolean active = Boolean.parseBoolean(data[1]);

                screen.updateInformation(event.getId(), volume, active);
            }
        } else if (Minecraft.getInstance().screen instanceof MusicControllerScreen screen) {
            if (action == MiscAction.MUSIC_CONTROLLER_GET) {
                String[] parts = Utils.split(event.getData(), "/");
                screen.updateInformation(event.getId(), MusicController.fromString(Minecraft.getInstance().level, event.getData()),
                        Utils.integerBooleanListOf(parts[4]), Boolean.parseBoolean(parts[5]));
            }
        }
    }
}
