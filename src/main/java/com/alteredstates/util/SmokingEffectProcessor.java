package com.alteredstates.util;

import com.alteredstates.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SmokingEffectProcessor {

    public static void applySmokingEffects(LivingEntity entity, boolean isIndica, int quality) {
        if (entity.level().isClientSide) return;

        // ⏱️ Efectos buenos: 45 segundos base por nivel de calidad (Ej: Calidad 3 = 2 minutos y pico)
        int goodDuration = 800 * quality;
        int amplifier = quality >= 3 ? 1 : 0;

        if (isIndica) {
            entity.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, goodDuration, amplifier));
            // Bonus Premium: Regeneración cortita
            if (quality >= 3) entity.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 300, 1));
        } else {
            entity.addEffect(new MobEffectInstance(ModEffects.SATIVA_EFFECT, goodDuration, amplifier));
            // Bonus Premium: Prisa minera cortita
            if (quality >= 3) entity.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 400, 1));
        }

        // 💀 Paranoia: Probabilidad base (10%), sube si la calidad es baja. Duración muy corta (15-30 segs)
        float paranoiaChance = quality <= 1 ? 0.20f : 0.05f;
        if (entity.level().random.nextFloat() < paranoiaChance) {
            int badDuration = 600 + (100 * quality); // 30 a 45 segundos
            entity.addEffect(new MobEffectInstance(ModEffects.PARANOIA, badDuration, 0));
        }
    }

    // Nuevo método exclusivo para el BONG
    public static void applyBongEffects(LivingEntity entity, boolean isIndica, int quality) {
        if (entity.level().isClientSide) return;

        // ⏱️ Efectos del Bong: Duran la MITAD que un porro, pero pegan el DOBLE de fuerte
        int bongDuration = 450 * quality; // Ej: Calidad 3 = 1 minuto aprox

        // El amplificador sube a Nivel 2 o 3 (Nivel 3 si es premium)
        int amplifier = quality >= 3 ? 2 : 1;

        if (isIndica) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.INDICA_EFFECT, bongDuration, amplifier));
            if (quality >= 2) entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 300, 1));
        } else {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.SATIVA_EFFECT, bongDuration, amplifier));
            if (quality >= 2) entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 600, 1));
        }

        // 💀 Paranoia del Bong: ¡Más peligrosa! Pega de golpe al cerebro
        float paranoiaChance = quality <= 1 ? 0.50f : (quality == 2 ? 0.30f : 0.15f);
        if (entity.level().random.nextFloat() < paranoiaChance) {
            int badDuration = 800 + (200 * quality);
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.PARANOIA, badDuration, 1)); // Paranoia nivel 2!
        }
    }

    public static void applyEdibleEffects(Player player, boolean isIndica, int quality) {
        int duration = 12000 * quality;
        int intensity = 1;

        if (isIndica) {
            player.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, duration, intensity, false, false, true));
        } else {
            // ⚠️ REVISA ESTA LÍNEA: Asegúrate de que pone SATIVA_EFFECT
            player.addEffect(new MobEffectInstance(ModEffects.SATIVA_EFFECT, duration, intensity, false, false, true));
        }
    }
}