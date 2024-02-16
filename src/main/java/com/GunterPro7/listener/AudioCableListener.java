package com.GunterPro7.listener;

import com.GunterPro7.entity.AudioCable;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11C;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT) // TODO implement server side
public class AudioCableListener {
    private static final List<AudioCable> audioCables = new ArrayList<>();
    @Nullable private BlockPos pos1;


    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (pos1 == null) {
            pos1 = event.getPos();
        } else {
            audioCables.add(new AudioCable(pos1, event.getPos(), DyeColor.getColor(event.getItemStack())));
        }
    }

    @SubscribeEvent
    public void renderLines(RenderLevelStageEvent event) {
        Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        for (AudioCable audioCable : audioCables) {
            BlockPos pos1 = audioCable.getStartPos();
            BlockPos pos2 = audioCable.getEndPos();

            float[] rgb = audioCable.getRGB();

            try (VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC)) {
                buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(pos1.getX(), pos1.getY(), pos1.getZ()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
                buffer.vertex(pos2.getX(), pos2.getY(), pos2.getZ()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
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
}
