package com.GunterPro7.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class MusicBoxBlock extends SlabBlock {
    public static final IntegerProperty VOLUME = IntegerProperty.create("musicboxes_volume", 0, 100);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("musicboxes_active");

    public MusicBoxBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE_SLAB).strength(3f));
        this.registerDefaultState(this.stateDefinition.any().setValue(VOLUME, 50));
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VOLUME, ACTIVE);
    }

}
