package com.GunterPro7.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MusicControllerBlock extends Block {
    public MusicControllerBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f));
    }
}
