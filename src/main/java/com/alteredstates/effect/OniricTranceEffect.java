package com.alteredstates.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class OniricTranceEffect extends MobEffect {

    public OniricTranceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {

            MobEffectInstance currentEffect = player.getEffect(com.alteredstates.registry.ModEffects.ONIRIC_TRANCE);
            if (currentEffect == null) return true;

            int duration = currentEffect.getDuration();

            // 🌙 1. EFECTOS DE TRANCE Y RELAJACIÓN PROFUNDA
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 55, amplifier + 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 220, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 55, Math.min(amplifier, 2), false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 55, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0, false, false, false));

            // 💖 2. REGENERACIÓN SUEÑO PROFUNDO
            if (player.getHealth() < player.getMaxHealth() && player.tickCount % 40 == 0) {
                player.heal(1.0F);
            }

            // ✨ 3. PARTÍCULAS ONÍRICAS (Aura roja y mística)
            if (duration % 10 == 0) {
                double px = player.getX();
                double py = player.getY() + 1.0;
                double pz = player.getZ();

                serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 4 + amplifier * 2, 0.4, 0.5, 0.4, 0.05);
                serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, px, py, pz, 3, 0.3, 0.4, 0.3, 0.02);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
