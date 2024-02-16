package com.GunterPro7.main;

import com.GunterPro7.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber
public class MusicBox {
    private static final Random random = new Random();

    public MusicBox() {

    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) throws IOException {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(System.getProperty("user.dir")));
        if (event.getPlacedBlock().getBlock().equals(Blocks.NOTE_BLOCK)) {
            FileManager.Positions.add(event.getPos());
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) throws IOException {
        if (event.getState().getBlock().equals(Blocks.NOTE_BLOCK)) {
            FileManager.Positions.remove(event.getPos());
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
    public void onTick(ServerChatEvent event) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        if (event.getMessage().getString().startsWith("musicBox")) {
            String message = event.getMessage().getString();

            String[] args = message.split(" ");

            SoundEvent soundEvent = null;
            if (args[1].equals("minecraft")) {
                soundEvent = discSounds.get(args[2]);
            } else if (args[1].equals("custom")) {
                ResourceLocation resourceLocation = new ResourceLocation(Main.MODID, args[2]);
                soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);
            }

            if (soundEvent == null) {
                event.getPlayer().sendSystemMessage(Component.literal("Couldn't recognize this sound!"));
                return;
            }

            PlayerList playerList = event.getPlayer().getServer().getPlayerList();
            Level level = event.getPlayer().level();
            for (BlockPos pos : FileManager.Positions.getAll()) {
                broadcastSound(playerList, level, pos.getX(), pos.getY(), pos.getZ(), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), SoundSource.RECORDS, 1f, 1f);
            }
        }
    }

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
