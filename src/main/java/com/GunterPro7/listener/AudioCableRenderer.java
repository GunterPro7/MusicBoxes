package com.GunterPro7.listener;

import com.GunterPro7.FileManager;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11C;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioCableRenderer {
    public static final List<AudioCable> audioCables = new ArrayList<>();
    @Nullable
    private Vec3 pos1;
    private long timePos1;
    @Nullable
    private BlockPos block1;

    private long lastId;
    private Vec3 preferredPos1;
    private BlockPos preferredBlockPos;

    private static VertexBuffer vertexBuffer;

    @SubscribeEvent
    public void onServerMessage(MiscNetworkEvent.ClientReceivedEvent event) {
        if (event.getAction() == MiscAction.AUDIO_CABLE_IS_FREE && event.getId() == lastId) {
            if (event.getData().startsWith("false")) {
                McUtils.sendPrivateChatMessage("This music box already has an Audio Cable connected!");
            } else {
                pos1 = preferredPos1;
                block1 = preferredBlockPos;
            }
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_REMOVE) {
            String[] parts = event.getData().split("/");
            for (String part : parts) {
                AudioCable audioCable = AudioCable.fromString(part);
                audioCables.remove(audioCable); // TODO test if there are no errors when creating an AudioCable
            }
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_NEW) {
            AudioCable audioCable = AudioCable.fromString(event.getData());
            audioCables.add(audioCable);
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_FETCH) {
            audioCables.clear();
            String[] parts = event.getData().split("/");
            for (String part : parts) {
                AudioCable audioCable = AudioCable.fromString(part);
                audioCables.add(audioCable); // TODO test if there are no errors when creating an AudioCable
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getEntity().getMainHandItem();
        itemStack = itemStack.is(Items.AIR) ? event.getEntity().getOffhandItem() : itemStack;

        if (itemStack.is(ModItems.MUSIC_CABLE_ITEM.get())) {
            Vec3 newPos = event.getHitVec().getLocation();
            if (System.currentTimeMillis() - timePos1 < 250 || (pos1 != null && pos1.equals(event.getHitVec().getLocation()))) return;

            //DyeColor dyeColor = DyeColor.getColor(event.getItemStack());
            DyeColor dyeColor = DyeColor.LIME; // TODO do color zuweisung
            if (dyeColor == null) return;

            lastId = Utils.getRandomId();
            MiscNetworkEvent.sendToServer(new MiscNetworkEvent(lastId, MiscAction.AUDIO_CABLE_IS_FREE, event.getPos().toShortString().replaceAll(", ", ",")));

            if (pos1 != null) {
                AudioCable audioCable = new AudioCable(pos1, newPos, block1, event.getPos(), event.getLevel(), dyeColor);
                if (audioCable.getBlockDistance() > 32d) {
                    McUtils.sendPrivateChatMessage("The Audio-wire cant be longer then 32 blocks!");
                } else {
                    audioCables.add(audioCable);
                    MiscNetworkEvent.sendToServer(Utils.getRandomId(), MiscAction.AUDIO_CABLE_POST, audioCable.toString());

                    pos1 = null;
                    block1 = null;
                    preferredPos1 = null;
                    preferredBlockPos = null;
                }
            } else {
                preferredPos1 = newPos;
                preferredBlockPos = event.getPos();
            }

            timePos1 = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void renderLines(RenderLevelStageEvent event) {
        if (Minecraft.getInstance().player == null) return;
        if (vertexBuffer == null) vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        for (AudioCable audioCable : audioCables) {
            if (audioCable.isInRange(Minecraft.getInstance().player.position(), 32d)) {
                renderLine(audioCable, view, buffer, event, VertexFormat.Mode.DEBUG_LINES);
            }

        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (pos1 != null) {
            DyeColor dyeColor = DyeColor.getColor(player.getMainHandItem());
            if (dyeColor == null) dyeColor = DyeColor.getColor(player.getOffhandItem());
            float[] rgb = dyeColor != null ? dyeColor.getTextureDiffuseColors() : new float[]{1f, 1f, 1f};

            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult instanceof BlockHitResult) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                if (Minecraft.getInstance().level != null && !Minecraft.getInstance().level.getBlockState(blockPos).is(Blocks.AIR)) {
                    if (pos1.closerThan(hitResult.getLocation(), 32d)) {
                        renderLine(pos1, hitResult.getLocation(), rgb, view, buffer, vertexBuffer, event, VertexFormat.Mode.DEBUG_LINE_STRIP);
                    }
                }
            }
        }
    }



    private void renderLine(AudioCable audioCable, Vec3 view, BufferBuilder buffer, RenderLevelStageEvent event, VertexFormat.Mode mode) {
        renderLine(audioCable.getStartPos(), audioCable.getEndPos(), audioCable.getRGB(), view, buffer, vertexBuffer, event, mode);
    }

    private void renderLine(Vec3 pos1, Vec3 pos2, float[] rgb, Vec3 view, BufferBuilder buffer, VertexBuffer vertexBuffer, RenderLevelStageEvent event, VertexFormat.Mode mode) {
        if (vertexBuffer != null) {
            buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(pos1.x(), pos1.y(), pos1.z()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
            buffer.vertex(pos2.x(), pos2.y(), pos2.z()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());

            PoseStack matrix = event.getPoseStack();
            matrix.pushPose();
            matrix.translate(-view.x, -view.y, -view.z);
            ShaderInstance shader = GameRenderer.getPositionColorShader();

            if (shader != null) {
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix(), shader);
                RenderSystem.disableDepthTest();
                matrix.popPose();

                VertexBuffer.unbind();
            }
        }
    }

}
