package com.GunterPro7.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MusicBoxBlock extends Block {

    public MusicBoxBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f));
    }
}
