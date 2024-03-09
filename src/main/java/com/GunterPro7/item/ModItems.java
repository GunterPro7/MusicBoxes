package com.GunterPro7.item;

import com.GunterPro7.main.Main;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<BlockItem> MUSIC_BOX_ITEM = registryObject("music_box", MusicBoxItem::new);
    public static final RegistryObject<BlockItem> MUSIC_CONTROLLER_ITEM = registryObject("music_controller", MusicControllerItem::new);

    private static <T extends BlockItem> RegistryObject<BlockItem> registryObject(String name, Supplier<T> blockItem){
        return ModItems.ITEMS.register(name, blockItem);
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
