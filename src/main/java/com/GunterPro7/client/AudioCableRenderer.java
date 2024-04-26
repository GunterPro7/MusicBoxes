package com.GunterPro7.client;

import com.GunterPro7.FileManager;
import com.GunterPro7.Main;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.item.ModItems;
import com.GunterPro7.item.MusicCableItem;
import com.GunterPro7.utils.ClientUtils;
import com.GunterPro7.utils.McUtils;
import com.GunterPro7.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
    private static VertexBuffer inline;

    private static final VertexFormat.Mode inlineMode = VertexFormat.Mode.QUADS;
    private static final VertexFormat.Mode outLineMode = VertexFormat.Mode.DEBUG_LINES;

    private static final double ls = 0.015625d;
    private static final double[][] dirs = {
            {-ls, +ls, -ls}, {-ls, -ls, -ls}, {-ls, -ls, -ls}, {-ls, +ls, -ls},
            {+ls, +ls, +ls}, {+ls, -ls, +ls}, {+ls, -ls, +ls}, {+ls, +ls, +ls},
            {+ls, +ls, +ls}, {-ls, +ls, -ls}, {-ls, +ls, -ls}, {+ls, +ls, +ls},
            {+ls, -ls, +ls}, {-ls, -ls, -ls}, {-ls, -ls, -ls}, {+ls, -ls, +ls},
    };

    @SubscribeEvent
    public void onServerMessage(MiscNetworkEvent.ClientReceivedEvent event) {
        if (event.getAction() == MiscAction.AUDIO_CABLE_IS_FREE && event.getId() == lastId) {
            if (event.getData().startsWith("false")) {
                ClientUtils.sendPrivateChatMessage("This music box already has an Audio Cable connected!");
            } else {
                pos1 = preferredPos1;
                block1 = preferredBlockPos;
            }
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_REMOVE) {
            String[] parts = event.getData().split("/");
            for (String part : parts) {
                AudioCable audioCable = AudioCable.fromString(part);
                audioCables.remove(audioCable);
            }
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_NEW) {
            AudioCable audioCable = AudioCable.fromString(event.getData());
            audioCables.add(audioCable);
        } else if (event.getAction() == MiscAction.AUDIO_CABLE_FETCH) {
            audioCables.clear();
            String[] parts = event.getData().split("/");
            for (String part : parts) {
                AudioCable audioCable = AudioCable.fromString(part);
                audioCables.add(audioCable);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getEntity().getMainHandItem();
        itemStack = itemStack.is(Items.AIR) ? event.getEntity().getOffhandItem() : itemStack;

        if (itemStack.is(ModItems.MUSIC_CABLE_ITEM.get())) {
            Vec3 newPos = event.getHitVec().getLocation();
            if (System.currentTimeMillis() - timePos1 < 250 || (pos1 != null && pos1.equals(event.getHitVec().getLocation())))
                return;

            int color = ((MusicCableItem) itemStack.getItem()).getColor(itemStack);

            lastId = Utils.getRandomId();
            MiscNetworkEvent.sendToServer(new MiscNetworkEvent(lastId, MiscAction.AUDIO_CABLE_IS_FREE, event.getPos().toShortString().replaceAll(", ", ",")));

            if (pos1 != null) {
                AudioCable audioCable = new AudioCable(pos1, newPos, block1, event.getPos(), event.getLevel(), color);
                if (audioCable.getBlockDistance() > 32d) {
                    ClientUtils.sendPrivateChatMessage("The Audio-wire cant be longer then 32 blocks!");
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        if (Minecraft.getInstance().player == null || Main.fileManager.valueByKeyAndName("config.txt", "musicCableVisibility").equals("Off")) return;

        if (vertexBuffer == null) vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        if (inline == null) inline = new VertexBuffer(VertexBuffer.Usage.STATIC);

        Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        for (AudioCable audioCable : audioCables) {
            if (audioCable.isInRange(Minecraft.getInstance().player.position(), 32d)) {
                renderLine(audioCable, view, buffer, event);
            }

        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (pos1 != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.is(Items.AIR)) itemStack = player.getOffhandItem();
            if (itemStack.is(ModItems.MUSIC_CABLE_ITEM.get())) {
                float[] rgb = McUtils.getRGB(((MusicCableItem) itemStack.getItem()).getColor(itemStack));

                HitResult hitResult = Minecraft.getInstance().hitResult;
                if (hitResult instanceof BlockHitResult) {
                    BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                    if (Minecraft.getInstance().level != null && !Minecraft.getInstance().level.getBlockState(blockPos).is(Blocks.AIR)) {
                        if (pos1.closerThan(hitResult.getLocation(), 32d)) {
                            renderLine(pos1, hitResult.getLocation(), rgb, view, buffer, vertexBuffer, event);
                        }
                    }
                }
            }
        }
    }


    private void renderLine(AudioCable audioCable, Vec3 view, BufferBuilder buffer, RenderLevelStageEvent event) {
        renderLine(audioCable.getStartPos(), audioCable.getEndPos(), audioCable.getRGB(), view, buffer, vertexBuffer, event);
    }

    private void renderLine(Vec3 pos1, Vec3 pos2, float[] rgb, Vec3 view, BufferBuilder bufferBuilder, VertexBuffer vertexBuffer, RenderLevelStageEvent event) {
        if (vertexBuffer != null) {
            if (Main.fileManager.valueByKeyAndName("config.txt", "musicCableVisibility").equals("Fancy")) {
                bufferBuilder.begin(inlineMode, DefaultVertexFormat.POSITION_COLOR);

                for (int i = 0; i < dirs.length; i += 4) {
                    drawSquare(bufferBuilder, pos1, pos2, rgb, i);
                    drawSquare(bufferBuilder, pos2, pos1, rgb, i);
                }

                vertexBuffer.bind();
                vertexBuffer.upload(bufferBuilder.end());
                renderVertexBuffer(vertexBuffer, event.getPoseStack(), view, event.getProjectionMatrix());

                bufferBuilder.begin(outLineMode, DefaultVertexFormat.POSITION_COLOR);

                float[] darkerColor = Utils.toDarkerColor(rgb);
                for (int i = 0; i < dirs.length; i += 4) {
                    drawLine(bufferBuilder, pos1, pos2, darkerColor, i);
                }

            } else {
                bufferBuilder.begin(outLineMode, DefaultVertexFormat.POSITION_COLOR);

                bufferBuilder.vertex(pos1.x(), pos1.y(), pos1.z()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
                bufferBuilder.vertex(pos2.x(), pos2.y(), pos2.z()).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();

            }

            vertexBuffer.bind();
            vertexBuffer.upload(bufferBuilder.end());
            renderVertexBuffer(vertexBuffer, event.getPoseStack(), view, event.getProjectionMatrix());
        }
    }

    private void renderVertexBuffer(VertexBuffer vertexBuffer, PoseStack matrix, Vec3 view, Matrix4f matrix4f) {
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        ShaderInstance shader = GameRenderer.getPositionColorShader();

        if (shader != null) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            vertexBuffer.drawWithShader(matrix.last().pose(), matrix4f, shader);
            RenderSystem.disableDepthTest();
            matrix.popPose();

            VertexBuffer.unbind();
        }
    }

    private void drawLine(BufferBuilder bufferBuilder, Vec3 pos1, Vec3 pos2, float[] rgb, int index) {
        bufferBuilder.vertex(pos1.x() + dirs[index][0], pos1.y() + dirs[index][1], pos1.z() + dirs[index][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
        bufferBuilder.vertex(pos2.x() + dirs[index + 3][0], pos2.y() + dirs[index + 3][1], pos2.z() + dirs[index + 3][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
    }

    private void drawSquare(BufferBuilder bufferBuilder, Vec3 pos1, Vec3 pos2, float[] rgb, int index) {
        bufferBuilder.vertex(pos1.x() + dirs[index][0], pos1.y() + dirs[index][1], pos1.z() + dirs[index][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
        bufferBuilder.vertex(pos1.x() + dirs[index + 1][0], pos1.y() + dirs[index + 1][1], pos1.z() + dirs[index + 1][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
        bufferBuilder.vertex(pos2.x() + dirs[index + 2][0], pos2.y() + dirs[index + 2][1], pos2.z() + dirs[index + 2][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
        bufferBuilder.vertex(pos2.x() + dirs[index + 3][0], pos2.y() + dirs[index + 3][1], pos2.z() + dirs[index + 3][2]).color(rgb[0], rgb[1], rgb[2], 1f).endVertex();
    }

}
