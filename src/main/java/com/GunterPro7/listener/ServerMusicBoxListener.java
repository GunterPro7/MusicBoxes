package com.GunterPro7.listener;

import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.main.FileManager;
import com.GunterPro7.main.Main;
import com.GunterPro7.utils.ChatUtils;
import com.GunterPro7.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.util.*;

@Mod.EventBusSubscriber
public class ServerMusicBoxListener {
    private static final Random random = new Random();

    private static final List<MusicBox> musicBoxes = new ArrayList<>();
    private static boolean musicBoxesLoaded;

    public ServerMusicBoxListener() {

    }

    public static MusicBox getMusicBoxByPos(BlockPos blockPos) {
        for (MusicBox musicBox : musicBoxes) {
            if (musicBox.getBlockPos().equals(blockPos)) {
                return musicBox;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        if (event.getPlacedBlock().getBlock().equals(Blocks.NOTE_BLOCK)) {
            FileManager.Positions.add(event.getPos());
            musicBoxes.add(new MusicBox(event.getPos()));
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) throws IOException {
        if (event.getState().getBlock().equals(Blocks.NOTE_BLOCK)) {
            BlockPos pos = event.getPos();

            FileManager.Positions.remove(pos);
            musicBoxes.remove(new MusicBox(pos));

            MusicBox musicBoxToDelete = null;
            for (MusicBox musicBox : musicBoxes) {
                if (pos.equals(musicBox.getBlockPos())) {
                    musicBoxToDelete = musicBox;
                }
            }

            if (musicBoxToDelete != null) {
                musicBoxes.remove(musicBoxToDelete);
            }
        }
    }

    private static final Map<String, SoundEvent> discSounds = MapUtils.of(
            "5", SoundEvents.MUSIC_DISC_5,
            "11", SoundEvents.MUSIC_DISC_11,
            "3", SoundEvents.MUSIC_DISC_13,
            "blocks", SoundEvents.MUSIC_DISC_BLOCKS,
            "cat", SoundEvents.MUSIC_DISC_CAT,
            "chirp", SoundEvents.MUSIC_DISC_CHIRP,
            "far", SoundEvents.MUSIC_DISC_FAR,
            "mall", SoundEvents.MUSIC_DISC_MALL,
            "mellohi", SoundEvents.MUSIC_DISC_MELLOHI,
            "pigstep", SoundEvents.MUSIC_DISC_PIGSTEP,
            "stal", SoundEvents.MUSIC_DISC_STAL,
            "strad", SoundEvents.MUSIC_DISC_STRAD,
            "wait", SoundEvents.MUSIC_DISC_WAIT,
            "ward", SoundEvents.MUSIC_DISC_WARD,
            "otherside", SoundEvents.MUSIC_DISC_OTHERSIDE,
            "relic", SoundEvents.MUSIC_DISC_RELIC
    );

    @SubscribeEvent
    public void realTICK(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().player == null) return;
        BlockPos playerPos = Minecraft.getInstance().player.getOnPos();

        BlockPos pos2 = new BlockPos(playerPos.getX() + 5, playerPos.getY(), playerPos.getZ());

        //renderLead(playerPos, pos2, 0.4f, 0.4f, 0.0f, 0.75f);
    }


    @SubscribeEvent
    public void onTick(ServerChatEvent event) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        if (event.getMessage().getString().startsWith("musicBox")) {
            String message = event.getMessage().getString();

            String[] args = message.split(" ");

            ResourceLocation resourceLocation = null;
            SoundEvent soundEvent = null;
            if (args[2].equals("minecraft")) {
                soundEvent = discSounds.get(args[3]);
                resourceLocation = soundEvent.getLocation();
            } else if (args[2].equals("custom")) {
                resourceLocation = new ResourceLocation(Main.MODID, args[3]);
                soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);
            }

            if (soundEvent == null) {
                ChatUtils.sendPrivateChatMessage("Couldn't recognize this sound!");
                return;
            }

            PlayerList playerList = event.getPlayer().getServer().getPlayerList();
            Level level = event.getPlayer().level();

            if (!musicBoxesLoaded) {
                for (BlockPos blockPos : FileManager.Positions.getAll()) {
                    musicBoxes.add(new MusicBox(blockPos));
                }
                musicBoxesLoaded = true;
            }

            //List<BlockPos> posList = new ArrayList<>();

            //for (MusicBox musicBox : musicBoxes) {
            //    if (musicBox.isPowered() && musicBox.getAudioCable().getColor().equals(DyeColor.valueOf(args[1].toUpperCase()))) { // or implement also for "all"
            //        if (MusicController.getMusicControllerByMusicBox(musicBox) != null) {
            //            posList.add(musicBox.getBlockPos());
            //        }
            //    }
            //}

            Set<MusicBox> musicBoxes = MusicController.getMusicBoxesByColorAndPos(new BlockPos(0, 100, 0), DyeColor.valueOf(args[1].toUpperCase()));
            // TODO this is not working, we get an empty array, alr added test messages


            Minecraft.getInstance().player.sendSystemMessage(Component.literal(musicBoxes.toString()));
            List<BlockPos> posList = musicBoxes.stream().map(MusicBox::getBlockPos).toList();
            for (ServerPlayer serverPlayer : playerList.getPlayers()) {
                sendToClient(serverPlayer, new ClientMusicBoxManager(!(args.length > 4 && args[4].equals("stop")), resourceLocation, posList));
            }
        }
    }

    public static List<MusicBox> getMusicBoxesContainingController(List<DyeColor> colors, boolean invert) {
        List<MusicBox> musicBoxes = new ArrayList<>();
        for (MusicBox musicBox : ServerMusicBoxListener.musicBoxes) {
            if (musicBox.isPowered() && colors.contains(musicBox.getAudioCable().getColor())) { // or implement also for "all"
                MusicController musicController = MusicController.getMusicControllerByMusicBox(musicBox);
                if (invert == (musicController == null)) {
                    musicBoxes.add(musicBox);
                }
            }
        }

        return musicBoxes;
    }

    public static void sendToClient(ServerPlayer player, ClientMusicBoxManager message) {
        ClientMusicBoxManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    @Deprecated
    private void broadcastSound(PlayerList players, Level level, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
        net.minecraftforge.event.PlayLevelSoundEvent.AtPosition event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtPosition(level, x, y, z, sound, source, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        source = event.getSource();
        volume = event.getNewVolume();
        pitch = event.getNewPitch();

        players.broadcast(null, x, y, z, Double.MAX_VALUE, level.dimension(), new ClientboundSoundPacket(sound, source, x, y, z, volume, pitch, random.nextLong()));
    }
}
