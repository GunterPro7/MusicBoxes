package com.GunterPro7.ui;

import com.GunterPro7.entity.MusicController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

public class MusicControllerScreen extends Screen {
    private final MusicController musicController;

    public MusicControllerScreen(MusicController musicController) {
        super(Component.literal("Music Box"));
        this.musicController = musicController;
    }

    @Override
    public void init() {
        super.init();
        int centerX = (this.width) / 2 - 50;
        int centerY = (this.height) / 2 - 50;

        this.addRenderableWidget(new Button.Builder(Component.literal("Clear Queue"),
                button -> musicController.getMusicQueue().clear())
                .bounds(centerX, centerY, 100, 20).build());


        this.addRenderableWidget(new StringWidget(centerX, centerY + 30, 100, 20, Component.literal("Add to the Queue!"), this.font));
        this.addRenderableWidget(new EditBox(this.font, centerX, centerY + 50, 100, 20, Component.literal("")));

        for (String track : musicController.getMusicQueue()) {
            // TODO fill this thing here
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
