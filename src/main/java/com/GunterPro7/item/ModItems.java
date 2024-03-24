package com.GunterPro7.item;

import com.GunterPro7.Main;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<BlockItem> MUSIC_BOX_ITEM = registryBlockItem("music_box", MusicBoxItem::new);
    public static final RegistryObject<BlockItem> MUSIC_CONTROLLER_ITEM = registryBlockItem("music_controller", MusicControllerItem::new);
    public static final RegistryObject<Item> MUSIC_CABLE_ITEM = registryItem("music_cable", MusicCableItem::new);

    private static <T extends BlockItem> RegistryObject<BlockItem> registryBlockItem(String name, Supplier<T> blockItem) {
        return ModItems.ITEMS.register(name, blockItem);
    }

    private static <T extends Item> RegistryObject<Item> registryItem(String name, Supplier<T> blockItem) {
        return ModItems.ITEMS.register(name, blockItem);
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
        eventBus.register(new ModItems());
    }

    @SubscribeEvent
    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((item, tintIndex) -> tintIndex == 0 ? ((MusicCableItem) item.getItem()).getColor(item) : 0xFFFFFF, ModItems.MUSIC_CABLE_ITEM.get());
    }
}
