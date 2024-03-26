package com.GunterPro7.ui;

import com.GunterPro7.entity.MusicController;
import com.GunterPro7.utils.ColorNameDetector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class MusicControllerScreen extends Screen {
    private static final int SCROLL_HEIGHT = 16;

    private final MusicController musicController;
    private int scrollOffset;
    private int contentHeight;

    public MusicControllerScreen(MusicController musicController) {
        super(Component.literal("Music Box"));
        this.musicController = musicController;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) { // pDelta +1 -> rauf; -1 -> runter
        int diff = scrollOffset - (int) pDelta * 16;
        if (diff > 0 && diff < contentHeight + this.height) {
            scrollOffset = diff;
            rebuildWidgets();
        }
        return true;
    }

    @Override
    public void init() { // TODO das alles in instanzen speichern und dann efinfach nur y updaten
        super.init();
        int centerX = (this.width) / 2 - 30;
        int centerY = (this.height) / 2;

        int offsetTop = 50 - scrollOffset;
        int index = 0;
        for (int color : musicController.getColorsConnected()) {
            EditBox editBox = new EditBox(this.font, centerX + 20, offsetTop, 100, 20, Component.literal(""));
            editBox.setValue(ColorNameDetector.getColorName(color));

            StringWidget string = new StringWidget(centerX - editBox.getWidth() / 20 + 10, offsetTop + (20 - this.font.lineHeight) / 2 + 1,
                    10, 10, Component.literal(index++ + ":").setStyle(Style.EMPTY.withColor(color).withBold(true)), this.font);

            Button button = new Button.Builder(musicController.isColorConnectionActive(color) ?
                    Component.literal("✓").setStyle(Style.EMPTY.withColor(DyeColor.LIME.getTextColor())) :
                    Component.literal("x").setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor())),
                    thisButton -> {
                        boolean enabled = musicController.switchColorConnection(color);
                        thisButton.setMessage(enabled ? Component.literal("✓").setStyle(Style.EMPTY.withColor(DyeColor.LIME.getTextColor())) :
                                Component.literal("x").setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor())));
                    })
                    .bounds(centerX - editBox.getWidth() / 20 - 20, offsetTop, 20, 20).build();

            this.addRenderableWidget(editBox);
            this.addRenderableWidget(string);
            this.addRenderableWidget(button);

            offsetTop += 25;
        }

        offsetTop += 50;

        this.addRenderableWidget(new StringWidget(centerX - 60, offsetTop, 55, 20, Component.literal("Currently running:"), this.font));

        this.addRenderableWidget(new Button.Builder(Component.literal(String.valueOf(musicController.getMusicQueue().getCurrentTrack())), thisButton -> {
            musicController.getMusicQueue().play(thisButton.getMessage().getString());
            rebuildWidgets();
        }).bounds(centerX + 20, offsetTop, 100, 20).build());

        offsetTop += 25;

        for (String track : musicController.getMusicQueue().tracks()) {
            Button buttonDelete = new Button.Builder(Component.literal("\uD83D\uDDD1"), thisButton -> {
                musicController.getMusicQueue().remove(track);
                rebuildWidgets();
            }).bounds(centerX - 5, offsetTop, 20, 20).build();
            Button buttonPlay = new Button.Builder(Component.literal(track), thisButton -> {
                musicController.getMusicQueue().play(track);
                rebuildWidgets();
            }).bounds(centerX + 20, offsetTop, 100, 20).build();

            this.addRenderableWidget(buttonDelete);
            this.addRenderableWidget(buttonPlay);
            offsetTop += 25;
        }

        EditBox newTrackEditBox = new EditBox(this.font, centerX + 20, offsetTop, 100, 20, Component.literal(""));
        Button buttonAdd = new Button.Builder(Component.literal("+"), thisButton -> {
            musicController.getMusicQueue().add(newTrackEditBox.getValue());
            newTrackEditBox.setValue("");
            rebuildWidgets();
        }).bounds(centerX - 5, offsetTop, 20, 20).build();

        this.addRenderableWidget(newTrackEditBox);
        this.addRenderableWidget(buttonAdd);





        //this.addRenderableWidget(new Button.Builder(Component.literal("Clear Queue"),
        //        button -> musicController.getMusicQueue().clear())
        //        .bounds(centerX, centerY, 100, 20).build());


        //this.addRenderableWidget(new StringWidget(centerX, centerY + 30, 100, 20, Component.literal("Add to the Queue!"), this.font));
        //this.addRenderableWidget(new EditBox(this.font, centerX, centerY + 50, 100, 20, Component.literal("")));

        //for (String track : musicController.getMusicQueue()) {
        //    // TODO fill this thing here
        //}

        this.contentHeight = offsetTop + this.height + 100;
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
