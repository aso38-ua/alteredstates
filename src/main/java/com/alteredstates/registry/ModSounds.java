package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, AlteredStates.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GRINDER_CRUSH = SOUND_EVENTS.register("item.grinder.crush",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "item.grinder.crush")));
}