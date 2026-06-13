package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.effect.ParanoiaEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    // El registro diferido principal para tus efectos
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, AlteredStates.MOD_ID);

    // Efecto Indica
    // 🔍 CORREGIDO: Cambiado 'EFFECTS.register' por 'MOB_EFFECTS.register'
    public static final DeferredHolder<MobEffect, MobEffect> INDICA_EFFECT = MOB_EFFECTS.register("indica_stoned",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x1E4620) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "indica_slowness"),
                            -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ARMOR,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "indica_resistance"),
                            3.0, AttributeModifier.Operation.ADD_VALUE)
    );

    // Efecto Sativa
    // 🔍 CORREGIDO: Cambiado 'EFFECTS.register' por 'MOB_EFFECTS.register'
    public static final DeferredHolder<MobEffect, MobEffect> SATIVA_EFFECT = MOB_EFFECTS.register("sativa_high",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x55FF55) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "sativa_speed"),
                            0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    // 🔴 Efecto Paranoia (Mal Viaje)
    // 🔍 CORREGIDO: Cambiado 'EFFECTS.register' por 'MOB_EFFECTS.register'
    public static final DeferredHolder<MobEffect, MobEffect> PARANOIA = MOB_EFFECTS.register("paranoia",
            () -> new ParanoiaEffect(MobEffectCategory.HARMFUL, 0x2A0835)
    );

    // Nuestro efecto para los comestibles
    public static final DeferredHolder<MobEffect, MobEffect> DIGESTING =
            MOB_EFFECTS.register("digesting", DigestingEffect::new);

    // Clase estática interna para el efecto de digestión
    public static class DigestingEffect extends MobEffect {
        public DigestingEffect() {
            super(MobEffectCategory.NEUTRAL, 0x5a3d28); // Color marrón
        }
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}