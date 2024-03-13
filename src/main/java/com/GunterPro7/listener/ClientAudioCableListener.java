package com.GunterPro7.listener;

import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.FileManager;
import com.GunterPro7.utils.McUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientAudioCableListener extends AudioCableListener {
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
                    MusicBox musicBox = ServerMusicBoxListener.getMusicBoxByPos(blockPos);
                    if (musicBox != null && musicBox.isPowered()) {
                        return;
                    }

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
