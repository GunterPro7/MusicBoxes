package com.GunterPro7;

import com.GunterPro7.entity.MusicBox;
import com.GunterPro7.entity.MusicController;
import com.GunterPro7.connection.MusicBoxEvent;
import com.GunterPro7.utils.MapUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// Server Side
public class MusicBoxesCommand {
    private static final Map<String, SoundEvent> discSounds = MapUtils.of(
            "5", SoundEvents.MUSIC_DISC_5,
            "11", SoundEvents.MUSIC_DISC_11,
            "3", SoundEvents.MUSIC_DISC_13,
            "blocks", SoundEvents.MUSIC_DISC_BLOCKS,
            "cat", SoundEvents.MUSIC_DISC_CAT,
            "chirp", SoundEvents.MUSIC_DISC_CHIRP,
            "far", SoundEvents.MUSIC_DISC_FAR,
            "mall", SoundEvents.MUSIC_DISC_MALL,
            "mellohi", SoundEvents.MUSIC_DISC_MELLOHI,
            "pigstep", SoundEvents.MUSIC_DISC_PIGSTEP,
            "stal", SoundEvents.MUSIC_DISC_STAL,
            "strad", SoundEvents.MUSIC_DISC_STRAD,
            "wait", SoundEvents.MUSIC_DISC_WAIT,
            "ward", SoundEvents.MUSIC_DISC_WARD,
            "otherside", SoundEvents.MUSIC_DISC_OTHERSIDE,
            "relic", SoundEvents.MUSIC_DISC_RELIC
    );

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("musicBox")
                .then(Commands.literal("controller")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .then(Commands.argument("color", IntegerArgumentType.integer())
                                                        .then(Commands.argument("Name of the Track", StringArgumentType.string())
                                                                .then(Commands.literal("stop")
                                                                        .executes(context -> controllerCommand(context, false)))
                                                                .then(Commands.literal("play")
                                                                        .executes(context -> controllerCommand(context, true)))))))))

                .then(Commands.literal("help")
                        .executes(MusicBoxesCommand::helpCommand)));
    }

    private static int controllerCommand(CommandContext<CommandSourceStack> context, boolean play) {
        MusicController musicController = MusicController.getController(new BlockPos(context.getArgument("x", Integer.class),
                context.getArgument("y", Integer.class), context.getArgument("z", Integer.class)));

        if (musicController != null) {
            int color = context.getArgument("color", Integer.class);
            String track = context.getArgument("Name of the Track", String.class);

            ResourceLocation resourceLocation;
            if (discSounds.containsKey(track)) {
                resourceLocation = discSounds.get(track).getLocation();
            } else {
                try {
                    resourceLocation = new ResourceLocation(Main.MODID, track);
                } catch (ResourceLocationException e) {
                    Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(Component.literal("Couldn't recognize this sound!"));
                    return 0;
                }
            }

            List<ServerPlayer> players = Objects.requireNonNull(context.getSource().getPlayer()).server.getPlayerList().getPlayers();

            List<MusicBox> musicBoxes = musicController.getMusicBoxesByColor(color).stream().filter(musicBox -> !play || musicBox.isActive()).toList();
            List<BlockPos> posList = musicBoxes.stream().map(MusicBox::getBlockPos).toList();
            List<Float> volumeList = musicBoxes.stream().map(MusicBox::getVolume).toList();

            musicController.play(players, new MusicBoxEvent(play, resourceLocation, posList, volumeList));
        }

        return 1;
    }

    private static int helpCommand(CommandContext<CommandSourceStack> context) {
        String[] args = context.getInput().split(" ");

        return 1;
    }
}
