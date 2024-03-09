package com.GunterPro7.item;

import com.GunterPro7.block.ModBlocks;
import com.GunterPro7.block.MusicBoxBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MusicBoxItem extends BlockItem {

    public MusicBoxItem() {
        super(ModBlocks.MUSIC_BOX_BLOCK.get(), new Item.Properties().stacksTo(64));
    }
}
