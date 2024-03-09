package com.GunterPro7.item;

import com.GunterPro7.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class MusicControllerItem extends BlockItem {
    public MusicControllerItem() {
        super(ModBlocks.MUSIC_CONTROLLER_BLOCK.get(), new Item.Properties().stacksTo(1));
    }
}
