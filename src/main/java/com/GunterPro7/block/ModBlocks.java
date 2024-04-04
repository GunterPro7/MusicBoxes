package com.GunterPro7.block;

import com.GunterPro7.Main;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    public static final RegistryObject<Block> MUSIC_BOX_BLOCK = registryObject("music_box", MusicBoxBlock::new);
    public static final RegistryObject<Block> MUSIC_CONTROLLER_BLOCK = registryObject("music_controller", MusicControllerBlock::new);

    public static final RegistryObject<BlockEntityType<MusicControllerBlockEntity>> MUSIC_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("music_controller", () -> BlockEntityType.Builder.of(MusicControllerBlockEntity::new, MUSIC_CONTROLLER_BLOCK.get()).build(null));

    private static <T extends Block> RegistryObject<T> registryObject(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
