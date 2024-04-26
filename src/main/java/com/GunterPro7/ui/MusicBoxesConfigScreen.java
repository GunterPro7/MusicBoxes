package com.GunterPro7.ui;

import com.GunterPro7.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class MusicBoxesConfigScreen extends Screen {
    private static final String fileKey = "config.txt";

    private double lastBoxLoudness = 25d;

    private ForgeSlider boxLoudnessSlider;
    private Button cableVisibilityButton;

    private MusicBoxesConfigScreen() {
        super(Component.literal("Music Boxes Config"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = (this.width) / 2;

        addRenderableWidget(new StringWidget(centerX - 25, 75, 50, 10, Component.literal("§lMusic Boxes Mod"), this.font));
        addRenderableWidget(new StringWidget(centerX - 20, 92, 40, 10, Component.literal("-> by GunterPro7"), this.font));

        addRenderableWidget(new StringWidget(centerX - 97, 150, 80, 20, Component.literal("Music Boxes Loudness:"), this.font));
        boxLoudnessSlider = new ForgeSlider(centerX + 10, 150, 100, 20, Component.literal(""), Component.literal("%"), 0d, 100d, 25d, 0.1d, -1, true);
        addRenderableWidget(boxLoudnessSlider);

        addRenderableWidget(new StringWidget(centerX - 90, 175, 80, 20, Component.literal("Music Cables Visible:"), this.font));
        cableVisibilityButton = new Button.Builder(Component.literal("Off"), thisButton -> {
            switch (thisButton.getMessage().getString()) {
                case "Off" -> thisButton.setMessage(Component.literal("Fast"));
                case "Fast" -> thisButton.setMessage(Component.literal("Fancy"));
                default -> thisButton.setMessage(Component.literal("Off"));
            }

            saveToFile();
        }).bounds(centerX + 10, 175, 100, 20).build();
        addRenderableWidget(cableVisibilityButton);

        readFromFile();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (boxLoudnessSlider.getValue() != lastBoxLoudness) {
            lastBoxLoudness = boxLoudnessSlider.getValue();
            saveToFile();
        }

        if (this.minecraft != null) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private void readFromFile() {
        try (BufferedReader reader = Main.fileManager.readerByKey(fileKey)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length > 1) {
                    switch (parts[0]) {
                        case "musicBoxLoudness":
                            double v;
                            try {
                                v = Double.parseDouble(parts[1]);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                v = 25;
                            }

                            boxLoudnessSlider.setValue(v);
                            lastBoxLoudness = v;
                            break;
                        case "musicCableVisibility":
                            cableVisibilityButton.setMessage(Component.literal(parts[1]));
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        if (boxLoudnessSlider != null && cableVisibilityButton != null) {
            try (BufferedWriter writer = Main.fileManager.writerByKey(fileKey)) {
                writer.write("musicBoxLoudness: " + boxLoudnessSlider.getValue() + "\n");
                writer.write("musicCableVisibility: " + cableVisibilityButton.getMessage().getString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static int display() {
        Minecraft.getInstance().setScreen(new MusicBoxesConfigScreen());

        return 1;
    }
}
