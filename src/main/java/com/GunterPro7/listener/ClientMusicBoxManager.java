package com.GunterPro7.listener;

import com.GunterPro7.main.Main;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientMusicBoxManager {
    private static final List<SoundInstance> instances = new ArrayList<>();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation.Serializer locationSerializer = new ResourceLocation.Serializer();

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // True = Play; False = Stop
    private final boolean play;
    private final ResourceLocation resourceLocation;
    private final List<BlockPos> posList;

    public ClientMusicBoxManager(FriendlyByteBuf buffer) {
        this.play = buffer.readBoolean();
        this.resourceLocation = locationSerializer.deserialize(new JsonPrimitive(buffer.readUtf(buffer.readShort())), null, null);

        posList = new ArrayList<>();
        for (String curPos : buffer.readUtf(buffer.readShort()).split(";")) {
            if (!curPos.isEmpty()) {
                posList.add(parsePosString(curPos));
            }
        }
    }

    public ClientMusicBoxManager(boolean play, ResourceLocation resourceLocation, List<BlockPos> posList) {
        this.resourceLocation = resourceLocation;
        this.posList = posList;
        this.play = play;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(play);

        String serialized = locationSerializer.serialize(resourceLocation, null, null).getAsString();
        buffer.writeShort(serialized.length());
        buffer.writeUtf(serialized);

        String posListString = blockPosListToString(posList);
        buffer.writeShort(posListString.length());
        buffer.writeUtf(posListString);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (play) {
                playSounds(resourceLocation, posList);
            } else {
                stopSounds(resourceLocation, posList);
            }
        });
        context.get().setPacketHandled(true);
    }

    public void playSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList) {
        removeInactiveSounds();

        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);

        for (BlockPos pos : blockPosList) {
            playSound(soundEvent, pos);
        }
    }

    private void removeInactiveSounds() {
        SoundManager soundManager = mc.getSoundManager();
        instances.removeIf(soundInstance -> !soundManager.isActive(soundInstance));
    }

    public void stopSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList) {
        removeInactiveSounds();

        SoundManager soundManager = mc.getSoundManager();
        for (SoundInstance soundInstance : instances) {
            if (soundInstance.getLocation().equals(resourceLocation)) {
                BlockPos blockPos = new BlockPos((int) soundInstance.getX(), (int) soundInstance.getY(), (int) soundInstance.getZ());
                if (blockPosList.contains(blockPos)) {
                    soundManager.stop(soundInstance);
                }
            }
        }
    }

    public void playSound(SoundEvent soundEvent, BlockPos pos) {
        SoundInstance soundInstance = new SimpleSoundInstance(soundEvent, SoundSource.RECORDS, 1f, 1f, SoundInstance.createUnseededRandom(), pos);
        instances.add(soundInstance);

        mc.getSoundManager().playDelayed(soundInstance, 2);
    }

    public BlockPos parsePosString(String posString) {
        String[] parts = posString.split(",");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private static String blockPosListToString(List<BlockPos> blockPosList) {
        StringBuilder stringBuilder = new StringBuilder();
        blockPosList.forEach(pos -> stringBuilder.append(blockPosToString(pos)).append(";"));

        return stringBuilder.toString();
    }

    private static String blockPosToString(BlockPos blockPos) {
        return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }
}
