package com.GunterPro7.ui;

import com.GunterPro7.entity.MusicBox;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;

public class MusicBoxScreen extends Screen {
    private final MusicBox musicBox;

    public MusicBoxScreen(MusicBox musicBox) {
        super(Component.literal("Music Box"));
        this.musicBox = musicBox;

        initScreen();
    }

    private void initScreen() {
        this.addRenderableWidget(new Button.Builder(Component.literal("test button"), button -> {
            System.out.println(button.getMessage().getString());
        }).bounds(500, 500, 100, 100).build());

        this.addRenderableWidget(new ForgeSlider(500, 500, 200, 200, Component.literal("test :) -> "), Component.literal("nothing"), 0d, 100d, 25d, 1d, -1, true));

        //this.addRenderableWidget(new EditBox(this.font, 500, 500, 200, 200, Component.literal("hey isn't this cool? :)")));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        //this.font.drawInBatch(":)", 500, 500, 0xffffff, true, new Matrix4f(), );
    }
}
