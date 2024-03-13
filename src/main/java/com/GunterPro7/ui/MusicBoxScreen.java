package com.GunterPro7.ui;

import com.GunterPro7.entity.MusicBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

public class MusicBoxScreen extends Screen {
    private final MusicBox musicBox;
    private ForgeSlider slider;

    public MusicBoxScreen(MusicBox musicBox) {
        super(Component.literal("Music Box"));
        this.musicBox = musicBox;
    }

    @Override
    public void init() {
        super.init();
        int centerX = (this.width) / 2 - 50;
        int centerY = (this.height) / 2 - 50;

        this.addRenderableWidget(new Button.Builder(Component.literal(musicBox.isActive() ? "§a§lEnabled" : "§c§lDisabled"),
                button -> {
                    boolean active = button.getMessage().getString().equals("§a§lEnabled");
                    button.setMessage(Component.literal(active ? "§c§lDisabled" : "§a§lEnabled"));
                    this.musicBox.setActive(!active);
                }).bounds(centerX, centerY, 100, 20).build());

        slider = new ForgeSlider(centerX, centerY + 30, 100, 20, Component.literal("Volume: "), Component.literal("%"), 0d, 100d, musicBox.getVolume(), 1d, -1, true);

        this.addRenderableWidget(slider);
        //this.addRenderableWidget(new EditBox(this.font, centerX, centerY + 60, 100, 20, Component.literal("")));

        this.addRenderableWidget(new StringWidget(centerX, 100, 100, 20, Component.literal("Music Box"), this.font));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            musicBox.setVolume(slider.getValue());
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        //this.font.drawInBatch(":)", 500, 500, 0xffffff, true, new Matrix4f(), );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
