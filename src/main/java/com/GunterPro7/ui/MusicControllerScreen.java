package com.GunterPro7.ui;

import com.GunterPro7.Main;
import com.GunterPro7.connection.MiscAction;
import com.GunterPro7.connection.MiscNetworkEvent;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.entity.MusicQueue;
import com.GunterPro7.entity.MusicTrack;
import com.GunterPro7.utils.ColorNameDetector;
import com.GunterPro7.utils.TimeUtils;
import com.GunterPro7.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicControllerScreen extends Screen {
    private static final int SCROLL_HEIGHT = 16;
    private final long interactionId;
    private Map<Integer, Boolean> colors = new HashMap<>();
    private MusicController musicController;
    private int scrollOffset;
    private int contentHeight;


    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<AbstractWidget> musicTypes = new ArrayList<>();
    private final Map<EditBox, MusicTrack> editBoxes = new HashMap<>();


    public MusicControllerScreen(long interactionId, MusicController musicController) {
        super(Component.literal("Music Controller"));
        this.musicController = musicController;
        this.interactionId = interactionId;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) { // pDelta +1 -> rauf; -1 -> runter
        int diff = scrollOffset - (int) pDelta * 16;
        if (diff > 0 && diff < contentHeight - this.height) {
            scrollOffset = diff;
            rescaleWidgets((int) pDelta * 16);
        }
        return true;
    }

    @Override
    public void rebuildWidgets() {
        widgets.clear();
        musicTypes.clear();
        super.rebuildWidgets();
    }

    public void rescaleWidgets(int diff) {
        widgets.forEach(widget -> widget.setY(widget.getY() + diff));
    }

    @Override
    public void init() {
        super.init();
        if (musicController != null) {
            int centerX = (this.width) / 2 - 30;
            int centerY = (this.height) / 2;

            int offsetTop = 50 - scrollOffset;
            int index = 0;

            MusicQueue musicQueue = musicController.getMusicQueue();


            for (Map.Entry<Integer, Boolean> colorInfo : colors.entrySet()) {
                int color = colorInfo.getKey();
                EditBox editBox = new EditBox(this.font, centerX + 20, offsetTop, 100, 20, Component.literal(""));
                editBox.setValue(ColorNameDetector.getColorName(color) + " Output");

                StringWidget string = new StringWidget(centerX - editBox.getWidth() / 20 + 10, offsetTop + (20 - this.font.lineHeight) / 2 + 1,
                        10, 10, Component.literal(index++ + ":").setStyle(Style.EMPTY.withColor(color).withBold(true)), this.font);

                Button button = new Button.Builder(colorInfo.getValue() ?
                        Component.literal("✓").setStyle(Style.EMPTY.withColor(DyeColor.LIME.getTextColor())) :
                        Component.literal("x").setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor())),
                        thisButton -> {
                            boolean enabled = !colors.get(color);
                            colors.put(color, enabled);
                            thisButton.setMessage(enabled ? Component.literal("✓").setStyle(Style.EMPTY.withColor(DyeColor.LIME.getTextColor())) :
                                    Component.literal("x").setStyle(Style.EMPTY.withColor(DyeColor.RED.getTextColor())));
                        })
                        .bounds(centerX - editBox.getWidth() / 20 - 20, offsetTop, 20, 20).build();

                widgets.add(button);
                widgets.add(string);
                widgets.add(editBox);

                offsetTop += 25;
            }

            offsetTop += 50;

            Button runningButton = new Button.Builder(Component.literal(musicQueue.isRunning() ? "⏸" : "⏯"), thisButton -> {
                if (musicQueue.switchRunning()) {
                    musicQueue.play();
                    thisButton.setMessage(Component.literal("⏸"));
                } else {
                    musicQueue.pause();
                    thisButton.setMessage(Component.literal("⏯"));
                }
            }).bounds(centerX - 5, offsetTop, 20, 20).build();
            widgets.add(runningButton);

            MusicTrack curTrack = musicQueue.getCurrentTrack();
            Button button = new Button.Builder(Component.literal(curTrack != null ? curTrack.getClientName() : "None"), thisButton -> {

                MusicTrack track = musicQueue.getTrackByName(thisButton.getMessage().getString());
                if (track != null) {
                    musicQueue.setRunning(true);
                    musicQueue.play(track);
                    rebuildWidgets();
                }

            }).bounds(centerX + 20, offsetTop, 100, 20).build();
            widgets.add(button);

            offsetTop += 25;

            Button repeat = new Button.Builder(Component.literal("\uD83D\uDD01")
                    .setStyle(Style.EMPTY.withBold(musicQueue.getPlayType() == MusicQueue.PlayType.REPEAT)), thisButton -> {
                musicController.getMusicQueue().setPlayType(MusicQueue.PlayType.REPEAT);
                switchMusicTypeBold(thisButton, musicTypes);
            }).bounds(centerX, offsetTop, 20, 20).build();

            Button repeat_track = new Button.Builder(Component.literal("\uD83D\uDD02")
                    .setStyle(Style.EMPTY.withBold(musicQueue.getPlayType() == MusicQueue.PlayType.REPEAT_TRACK)), thisButton -> {
                musicController.getMusicQueue().setPlayType(MusicQueue.PlayType.REPEAT_TRACK);
                switchMusicTypeBold(thisButton, musicTypes);
            }).bounds(centerX + 25, offsetTop, 20, 20).build();

            Button random = new Button.Builder(Component.literal("\uD83D\uDD00")
                    .setStyle(Style.EMPTY.withBold(musicQueue.getPlayType() == MusicQueue.PlayType.RANDOM)), thisButton -> {
                musicController.getMusicQueue().setPlayType(MusicQueue.PlayType.RANDOM);
                switchMusicTypeBold(thisButton, musicTypes);
            }).bounds(centerX + 50, offsetTop, 20, 20).build();

            musicTypes.add(repeat);
            musicTypes.add(repeat_track);
            musicTypes.add(random);

            widgets.add(repeat);
            widgets.add(repeat_track);
            widgets.add(random);

            offsetTop += 25;

            for (MusicTrack track : musicController.getMusicQueue().tracks()) {
                Button buttonDelete = new Button.Builder(Component.literal("\uD83D\uDDD1"), thisButton -> {
                    musicController.getMusicQueue().remove(track);
                    rebuildWidgets();
                }).bounds(centerX - 5, offsetTop, 20, 20).build();

                Button buttonSource = new Button.Builder(Component.literal(track.isCustomSound() ? "custom" : "MC"), thisButton -> {
                    thisButton.setMessage(Component.literal(track.switchCustomSound() ? "custom" : "MC"));
                }).bounds(centerX + 20, offsetTop, 35, 20).build();

                Button buttonPlay = new Button.Builder(Component.literal(track.getClientName()), thisButton -> {
                    musicQueue.setRunning(true);
                    musicController.getMusicQueue().play(track);
                    rebuildWidgets();
                }).bounds(centerX + 60, offsetTop, 100, 20).build();

                EditBox setTime = new EditBox(this.font, centerX + 165, offsetTop, 50, 20, Component.literal(""));
                setTime.setValue(String.valueOf(track.getLengthInSec()));



                editBoxes.put(setTime, track);

                widgets.add(buttonDelete);
                widgets.add(buttonSource);
                widgets.add(buttonPlay);
                widgets.add(setTime);
                offsetTop += 25;
            }


            EditBox newTrackEditBox = new EditBox(this.font, centerX + 60, offsetTop, 100, 20, Component.literal(""));
            EditBox newTrackTimeEditBox = new EditBox(this.font, centerX + 165, offsetTop, 50, 20, Component.literal(""));
            Button buttonAddSource = new Button.Builder(Component.literal("MC"), thisButton -> {
                thisButton.setMessage(Component.literal(thisButton.getMessage().getString().equals("MC") ? "custom" : "MC"));
            }).bounds(centerX + 20, offsetTop, 35, 20).build();

            Button buttonAdd = new Button.Builder(Component.literal("+"), thisButton -> {
                musicController.getMusicQueue().add(new MusicTrack(newTrackEditBox.getValue(),
                        buttonAddSource.getMessage().getString().equals("custom"),
                        Integer.parseInt(Utils.removeNonNumberChars(newTrackTimeEditBox.getValue()))));
                newTrackEditBox.setValue("");
                rebuildWidgets();
            }).bounds(centerX - 5, offsetTop, 20, 20).build();

            widgets.add(buttonAdd);
            widgets.add(buttonAddSource);
            widgets.add(newTrackEditBox);
            widgets.add(newTrackTimeEditBox);

            widgets.forEach(this::addRenderableWidget);

            this.contentHeight = offsetTop + 100 + scrollOffset;
        }
    }

    private void switchMusicTypeBold(Button thisButton, List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            widget.setMessage(widget.getMessage().copy().setStyle(Style.EMPTY.withBold(false)));
        }

        thisButton.setMessage(thisButton.getMessage().copy().setStyle(Style.EMPTY.withBold(true)));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean b = super.keyPressed(pKeyCode, pScanCode, pModifiers);

        TimeUtils.addJob(-1, 0, id -> {
            for (Map.Entry<EditBox, MusicTrack> editBox : editBoxes.entrySet()) {
                if (editBox.getKey().isFocused()) {
                    String v = Utils.removeNonNumberChars(editBox.getKey().getValue());
                    editBox.getKey().setValue(v);

                    if (!v.isEmpty()) {
                        editBox.getValue().setLengthInSec(Integer.parseInt(v));
                    }
                }
            }
        });

        return b;
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

    @Override
    public void onClose() {
        super.onClose();
        updateServer();
    }

    public void updateServer() {
        if (musicController != null) {
            MiscNetworkEvent.sendToServer(-1, MiscAction.MUSIC_CONTROLLER_INNER_UPDATE, musicController + "/" +
                    Utils.integerBooleanListToString(colors) + "/" + musicController.getMusicQueue().isRunning());
        }
    }

    public void updateInformation(long id, MusicController musicController, Map<Integer, Boolean> colors, boolean running) {
        if (this.interactionId == id) {
            this.musicController = musicController;
            this.colors = colors;
            this.musicController.getMusicQueue().setRunning(running);
            this.rebuildWidgets();
        }
    }
}
