package com.GunterPro7.ui;

import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

public class MusicBoxScreen extends Screen {
    public final BlockPos pos;
    public final long interactionId;

    private ForgeSlider slider;
    private Button button;
    public boolean newActive;

    private float lastSliderValue;
    private long lastCheck;

    public MusicBoxScreen(BlockPos pos, long id) {
        super(Component.literal("Music Box"));
        this.interactionId = id;
        this.pos = pos;
        this.newActive = true;
    }

    @Override
    public void init() {
        super.init();
        int centerX = (this.width) / 2 - 50;
        int centerY = (this.height) / 2 - 50;

        button = new Button.Builder(Component.literal(newActive ? "§a§lEnabled" : "§c§lDisabled"),
                cButton -> {
                    cButton.setMessage(Component.literal(newActive ? "§c§lDisabled" : "§a§lEnabled"));
                    this.newActive = !newActive;
                    updateServer();
                }).bounds(centerX, centerY, 100, 20).build();

        this.addRenderableWidget(button);

        slider = new ForgeSlider(centerX, centerY + 30, 100, 20, Component.literal("Volume: "), Component.literal("%"), 0d, 100d, 0d, 0.1d, -1, true);

        this.addRenderableWidget(slider);

        this.addRenderableWidget(new StringWidget(centerX, 100, 100, 20, Component.literal("Music Box"), this.font));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        if (System.currentTimeMillis() - lastCheck >= 1000) {
            if (lastSliderValue != (float) slider.getValue()) {
                lastSliderValue = (float) slider.getValue();
                updateServer();
            }
            lastCheck = System.currentTimeMillis();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        updateServer();
        MiscNetworkEvent.sendToServer(interactionId, MiscAction.MUSIC_BOX_INNER_EXIT, "");
    }

    public void updateServer() {
        MiscNetworkEvent.sendToServer(interactionId, MiscAction.MUSIC_BOX_INNER_UPDATE,
                pos.toShortString().replace(", ", ",")
                        + "/" + (float) slider.getValue() + "/" + newActive);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void updateInformation(long id, float volume, boolean active) {
        if (this.interactionId == id) {
            lastSliderValue = volume;
            slider.setValue(volume);
            newActive = active;
            button.setMessage(Component.literal(active ? "§a§lEnabled" : "§c§lDisabled"));
        }
    }
}
