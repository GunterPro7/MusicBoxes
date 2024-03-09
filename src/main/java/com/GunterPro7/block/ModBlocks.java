package com.GunterPro7.block;

import com.GunterPro7.main.Main;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);


    public static final RegistryObject<Block> MUSIC_BOX_BLOCK = registryObject("music_box", MusicBoxBlock::new);
    public static final RegistryObject<Block> MUSIC_CONTROLLER_BLOCK = registryObject("music_controller", MusicControllerBlock::new);

    private static <T extends Block> RegistryObject<T> registryObject(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus );
    }
}
