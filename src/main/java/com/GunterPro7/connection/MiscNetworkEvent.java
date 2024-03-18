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
    private final MiscAction action;
    private final String data;

    public MiscNetworkEvent(FriendlyByteBuf buffer) {
        this.id = buffer.readLong();
        this.action = MiscAction.valueOf(buffer.readByte());
        this.data = buffer.readUtf(buffer.readShort());
    }

    public MiscNetworkEvent(String data, long id, MiscAction action) {
        this.data = data;
        this.id = id;
        this.action = action;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(id);
        buffer.writeByte(action.id);
        buffer.writeShort(data.length());
        buffer.writeUtf(data, data.length());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (!context.get().getDirection().getOriginationSide().isClient()) {
                MinecraftForge.EVENT_BUS.post(new ClientReceivedEvent(id, action, data));
            } else {
                MinecraftForge.EVENT_BUS.post(new ServerReceivedEvent(id, action, data, context.get().getSender()));
            }
        });
        context.get().setPacketHandled(true);
    }

    public static class ClientReceivedEvent extends Event {
        private final long id;
        private final MiscAction action;
        private final String data;

        public ClientReceivedEvent(long id, MiscAction action, String data) {
            this.id = id;
            this.action = action;
            this.data = data;
        }

        public long getId() {
            return id;
        }

        public MiscAction getAction() {
            return this.action;
        }

        public String getData() {
            return data;
        }
    }

    public static class ServerReceivedEvent extends Event {
        private final long id;
        private final MiscAction action;
        private final String data;
        private final ServerPlayer player;

        public ServerReceivedEvent(long id, MiscAction action, String data, ServerPlayer player) {
            this.id = id;
            this.action = action;
            this.data = data;
            this.player = player;
        }

        public long getId() {
            return id;
        }

        public MiscAction getAction() {
            return this.action;
        }

        public String getData() {
            return data;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }

    public static void sendToClient(ServerPlayer player, long id, MiscAction action, String data) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MiscNetworkEvent(data, id, action));
    }

    public static void sendToServer(long id, MiscAction action, String data) {
        INSTANCE.sendToServer(new MiscNetworkEvent(data, id, action));
    }
}
