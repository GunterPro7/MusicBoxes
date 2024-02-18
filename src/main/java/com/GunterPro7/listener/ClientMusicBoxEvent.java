package com.GunterPro7.listener;

import com.GunterPro7.main.Main;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientMusicBoxEvent {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private final String json;

    public ClientMusicBoxEvent(FriendlyByteBuf buffer) {
        this.json = buffer.readUtf(32767);
    }

    public ClientMusicBoxEvent(String json) {
        this.json = json;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.json);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))) {

                short resourceLocationLength = inputStream.readShort();
                String resourceLocationString = new String(inputStream.readNBytes(resourceLocationLength));
                ResourceLocation resourceLocation = new ResourceLocation.Serializer().deserialize(new JsonPrimitive(resourceLocationString), null, null);

                List<BlockPos> posList = new ArrayList<>();
                short posListLength = inputStream.readShort();
                String posListString = new String(inputStream.readNBytes(posListLength));
                for (String curPos : posListString.split(";")) {
                    if (!curPos.isEmpty()) {
                        posList.add(parsePosString(curPos));
                    }
                }
                playSounds(resourceLocation, posList);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        context.get().setPacketHandled(true);
    }

    public void playSounds(ResourceLocation resourceLocation, List<BlockPos> blockPosList) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(resourceLocation);

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        for (BlockPos pos : blockPosList) {
            playSound(soundEvent, pos, level);
        }
    }

    public void playSound(SoundEvent soundEvent, BlockPos pos, Level level) {
        level.playLocalSound(pos, soundEvent, SoundSource.RECORDS, 1f, 1f, true); // TODO minecraft.getSoundManager().play(soundInstance); und soundInstance in liste haun EZ
    }

    public BlockPos parsePosString(String posString) {
        String[] parts = posString.split(",");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
