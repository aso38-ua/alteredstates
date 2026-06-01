package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.effect.ParanoiaEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AlteredStates.MOD_ID);

    // Efecto Indica
    public static final DeferredHolder<MobEffect, MobEffect> INDICA_EFFECT = EFFECTS.register("indica_stoned",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x1E4620) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "indica_slowness"),
                            -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ARMOR,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "indica_resistance"),
                            3.0, AttributeModifier.Operation.ADD_VALUE)
    );

    // Efecto Sativa
    public static final DeferredHolder<MobEffect, MobEffect> SATIVA_EFFECT = EFFECTS.register("sativa_high",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x55FF55) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "sativa_speed"),
                            0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    // 🔴 Efecto Paranoia (Mal Viaje): Color morado oscuro/negro
    public static final DeferredHolder<MobEffect, MobEffect> PARANOIA = EFFECTS.register("paranoia",
            () -> new ParanoiaEffect(MobEffectCategory.HARMFUL, 0x2A0835)
    );

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}