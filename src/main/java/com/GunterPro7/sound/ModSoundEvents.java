package com.GunterPro7.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "musicboxes", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "musicboxes");
    public static final RegistryObject<SoundEvent> CUSTOM1 = SOUNDS.register("custom2", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("musicboxes", "custom2")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
