package com.GunterPro7.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MusicBoxBlock extends SlabBlock {
    public MusicBoxBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE_SLAB).strength(3f));
    }
}
