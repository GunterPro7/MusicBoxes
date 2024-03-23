package com.GunterPro7.connection;

import com.GunterPro7.Main;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MusicBoxEvent {
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
    private final List<Float> volumeList;

    public MusicBoxEvent(FriendlyByteBuf buffer) {
        this.play = buffer.readBoolean();
        if (buffer.readBoolean()) {
            this.resourceLocation = locationSerializer.deserialize(new JsonPrimitive(buffer.readUtf(buffer.readShort())), null, null);
        } else {
            this.resourceLocation = null;
        }

        posList = new ArrayList<>();
        for (String curPos : buffer.readUtf(buffer.readShort()).split(";")) {
            if (!curPos.isEmpty()) {
                posList.add(parsePosString(curPos));
            }
        }

        this.volumeList = floatListFromString(buffer.readUtf(buffer.readShort()));
    }

    public MusicBoxEvent(boolean play, ResourceLocation resourceLocation, List<BlockPos> posList, List<Float> volumeList) {
        this.resourceLocation = resourceLocation;
        this.posList = posList;
        this.volumeList = volumeList;
        this.play = play;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(play);
        buffer.writeBoolean(resourceLocation != null);

        if (resourceLocation != null) {
            String serialized = locationSerializer.serialize(resourceLocation, null, null).getAsString();
            buffer.writeShort(serialized.length());
            buffer.writeUtf(serialized);
        }

        String posListString = blockPosListToString(posList);
        buffer.writeShort(posListString.length());
        buffer.writeUtf(posListString);

        String floatListString = floatListToString(volumeList);
        buffer.writeShort(floatListString.length());
        buffer.writeUtf(floatListString);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (play) {
                playSounds(resourceLocation, posList, volumeList);
            } else {
                ClientBoxEventHandler.stopSounds(resourceLocation, posList);
            }
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void playSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList, List<Float> volumeList) {
        ClientBoxEventHandler.removeInactiveSounds();

        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);

        for (int i = 0; i < blockPosList.size(); i++) {
            BlockPos pos = blockPosList.get(i);
            float volume = volumeList.get(i);

            ClientBoxEventHandler.playSound(soundEvent, pos, volume / 75);
        }
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

    private static String floatListToString(List<Float> floatList) {
        StringBuilder stringBuilder = new StringBuilder();
        floatList.forEach(curFloat -> stringBuilder.append(curFloat).append(";"));

        return stringBuilder.toString();
    }

    private static List<Float> floatListFromString(String floatListString) {
        List<Float> floatList = new ArrayList<>();
        for (String curFloat : floatListString.split(";")) {
            if (!curFloat.isEmpty())
                floatList.add(Float.valueOf(curFloat));
        }

        return floatList;
    }
}
