package com.GunterPro7.ui;

import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.MusicBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

public class MusicBoxScreen extends Screen {
    public final MusicBox musicBox;
    public final long id;

    private ForgeSlider slider;
    public boolean newActive;

    public MusicBoxScreen(MusicBox musicBox, long id) {
        super(Component.literal("Music Box"));
        this.id = id;
        this.musicBox = musicBox;
        this.newActive = musicBox.isActive();
    }

    @Override
    public void init() {
        super.init();
        int centerX = (this.width) / 2 - 50;
        int centerY = (this.height) / 2 - 50;

        this.addRenderableWidget(new Button.Builder(Component.literal(musicBox.isActive() ? "§a§lEnabled" : "§c§lDisabled"),
                button -> {
                    button.setMessage(Component.literal(newActive ? "§c§lDisabled" : "§a§lEnabled"));
                    this.newActive = !newActive;
                }).bounds(centerX, centerY, 100, 20).build());

        slider = new ForgeSlider(centerX, centerY + 30, 100, 20, Component.literal("Volume: "), Component.literal("%"), 0d, 100d, musicBox.getVolume(), 1d, -1, true);

        this.addRenderableWidget(slider);

        this.addRenderableWidget(new StringWidget(centerX, 100, 100, 20, Component.literal("Music Box"), this.font));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            musicBox.setVolume(slider.getValue());
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onClose() {
        super.onClose();

        if (musicBox.getVolume() != slider.getValue() || musicBox.isActive() != this.newActive) {
            MiscNetworkEvent.sendToServer("musicBox/update/" + slider.getValue() + "/" + newActive, -1);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
