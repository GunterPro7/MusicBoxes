package com.GunterPro7.connection;

import com.GunterPro7.Main;
import com.GunterPro7.utils.McUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class MiscNetworkEvent {
    private static final String PROTOCOL_VERSION = "1.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "miscpackets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private final long id;
    private final String data;

    public MiscNetworkEvent(FriendlyByteBuf buffer) {
        this.id = buffer.readLong();
        this.data = buffer.readUtf(buffer.readShort());
    }

    public MiscNetworkEvent(String data, long id) {
        this.data = data;
        this.id = id;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(id);
        buffer.writeShort(data.length());
        buffer.writeUtf(data, data.length());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (McUtils.isServerSide()) {
                MinecraftForge.EVENT_BUS.post(new ClientReceivedEvent(id, data));
            } else {
                MinecraftForge.EVENT_BUS.post(new ServerReceivedEvent(id, data, context.get().getSender()));
            }
        });
        context.get().setPacketHandled(true);
    }

    public static class ClientReceivedEvent extends Event {
        private final long id;
        private final String data;

        public ClientReceivedEvent(long id, String data) {
            this.id = id;
            this.data = data;
        }

        public long getId() {
            return id;
        }

        public String getData() {
            return data;
        }
    }

    public static class ServerReceivedEvent extends Event {
        private final long id;
        private final String data;
        private final ServerPlayer player;

        public ServerReceivedEvent(long id, String data, ServerPlayer player) {
            this.id = id;
            this.data = data;
            this.player = player;
        }

        public long getId() {
            return id;
        }

        public String getData() {
            return data;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }

    public static void sendToClient(ServerPlayer player, String data, long id) {
        MusicBoxEvent.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MiscNetworkEvent(data, id));
    }

    public static void sendToServer(String data, long id) {
        MusicBoxEvent.INSTANCE.sendToServer(new MiscNetworkEvent(data, id));
    }
}
