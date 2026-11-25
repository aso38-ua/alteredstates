package com.alteredstates.util;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class EffectUtil {
    private EffectUtil() {}

    /** Reaplica solo si no existe, tiene menos amplificador o le quedan <= refreshBelow ticks. */
    public static void refresh(LivingEntity entity, Holder<MobEffect> effect,
                               int duration, int amplifier, int refreshBelow) {
        refresh(entity, effect, duration, amplifier, refreshBelow, false, false, false);
    }

    public static void refresh(LivingEntity entity, Holder<MobEffect> effect,
                               int duration, int amplifier, int refreshBelow,
                               boolean ambient, boolean visible, boolean showIcon) {
        MobEffectInstance cur = entity.getEffect(effect);
        if (cur != null && cur.getAmplifier() >= amplifier
                && (cur.isInfiniteDuration() || cur.getDuration() > refreshBelow)) {
            return;
        }
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon));
    }
}