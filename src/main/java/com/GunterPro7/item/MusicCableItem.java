package com.GunterPro7.item;

import com.GunterPro7.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class MusicCableItem extends BlockItem {
    public MusicCableItem() { // TODO
        super(ModBlocks.MUSIC_BOX_BLOCK.get(), new Item.Properties().stacksTo(64));
    }
}