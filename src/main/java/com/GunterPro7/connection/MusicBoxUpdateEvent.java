package com.GunterPro7.connection;

import com.GunterPro7.FileManager;
import com.GunterPro7.Main;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.listener.ServerMusicBoxListener;
import com.GunterPro7.utils.McUtils;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.IOException;
import java.util.function.Supplier;

public class MusicBoxUpdateEvent {
    private static final ResourceLocation.Serializer locationSerializer = new ResourceLocation.Serializer();
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "updateboxevent"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private final ResourceLocation resourceLocation;
    private final BlockPos blockPos;
    private final double newVolume;
    private final boolean newActive;

    public MusicBoxUpdateEvent(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            this.resourceLocation = locationSerializer.deserialize(new JsonPrimitive(buffer.readUtf(buffer.readShort())), null, null);
        } else {
            this.resourceLocation = null;
        }

        this.blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        this.newVolume = buffer.readDouble();
        this.newActive = buffer.readBoolean();
    }

    public MusicBoxUpdateEvent(ResourceLocation resourceLocation, BlockPos blockPos, double volume, boolean active) {
        this.resourceLocation = resourceLocation;
        this.blockPos = blockPos;
        this.newVolume = volume;
        this.newActive = active;
    }

    public void encode(FriendlyByteBuf buffer) {
        String serialized = locationSerializer.serialize(resourceLocation, null, null).getAsString();
        buffer.writeShort(serialized.length());
        buffer.writeUtf(serialized);

        buffer.writeInt(blockPos.getX());
        buffer.writeInt(blockPos.getY());
        buffer.writeInt(blockPos.getZ());
        buffer.writeDouble(newVolume);
        buffer.writeBoolean(newActive);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (McUtils.isServerSide()) {
                MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(blockPos);
                if (musicBox != null) {
                    musicBox.setVolume(newVolume);
                    musicBox.setActive(newActive);
                    try {
                        FileManager.Positions.update(musicBox);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {

            }
        });
        context.get().setPacketHandled(true);
    }
}
