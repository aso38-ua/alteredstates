package com.alteredstates.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PsychodeliaEffect extends MobEffect {

    public PsychodeliaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {

            // CORRECCIÓN: Pasamos el DeferredHolder directamente sin el .get() para cumplir con el Holder<MobEffect>
            MobEffectInstance currentEffect = player.getEffect(com.alteredstates.registry.ModEffects.PSYCHODELIA);
            if (currentEffect == null) return true;

            int duration = currentEffect.getDuration();

            // ==========================================
            // 💀 1. EL OSTIÓN FINAL (Últimos 5 segundos = 100 ticks)
            // ==========================================
            if (duration <= 100) {
                player.removeEffect(MobEffects.SLOW_FALLING);
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 5, 2, false, false, false));
                serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY(), player.getZ(), 2, 0.2, 0.2, 0.2, 0.05);
                return true;
            }

            // ==========================================
            // 🌌 2. EFECTOS FÍSICOS Y DE POTENCIA (Escalados por amplificador)
            // ==========================================
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 15, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 15, amplifier, false, false, false));

            // Prisa minera (Haste) escalable según la calidad (Amp 0 = Haste I, Amp 2 = Haste III)
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 15, amplifier, false, false, false));

            // ⭐ EFECTO EXTRA PREMIUM: Si el amplificador es 2 (Calidad Premium de la seta seca)
            if (amplifier >= 2) {
                // Regeneración de vida mística constante mientras dura el viaje limpio
                if (player.getHealth() < player.getMaxHealth() && player.tickCount % 30 == 0) {
                    player.heal(1.0F);
                }
            }

            // 🥴 MOVIMIENTO INVOLUNTARIO (Tambaleo aleatorio)
            if (player.getRandom().nextFloat() < 0.08f && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
                double wobbleX = (player.getRandom().nextFloat() - 0.5D) * 0.22D;
                double wobbleZ = (player.getRandom().nextFloat() - 0.5D) * 0.22D;
                player.setDeltaMovement(player.getDeltaMovement().add(wobbleX, 0, wobbleZ));
                player.hurtMarked = true;
            }

            // ==========================================
            // 🌈 3. EFECTOS VISUALES EXAGERADOS (Cada 5 ticks)
            // ==========================================
            if (duration % 5 == 0) {
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();

                // Estela del propio jugador
                int myParticles = 4 + (amplifier * 3);
                serverLevel.sendParticles(ParticleTypes.ENCHANT, px, py + player.getEyeHeight(), pz, myParticles, 0.4, 0.4, 0.4, 0.1);
                serverLevel.sendParticles(ParticleTypes.CHERRY_LEAVES, px, py + 1.0, pz, myParticles / 2, 0.3, 0.5, 0.3, 0.05);

                // Wallhack de entidades vivas en base al rango de calidad
                int radius = 15 + (amplifier * 5);
                AABB boundingBox = player.getBoundingBox().inflate(radius);
                List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox);

                for (LivingEntity entityAround : nearbyEntities) {
                    if (entityAround == player) continue;

                    entityAround.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15, 0, false, false, false));

                    double ex = entityAround.getX();
                    double ey = entityAround.getY() + (entityAround.getBbHeight() / 2.0D);
                    double ez = entityAround.getZ();

                    serverLevel.sendParticles(ParticleTypes.GLOW, ex, ey, ez, 2, 0.3, 0.3, 0.3, 0.02);
                    serverLevel.sendParticles(ParticleTypes.WITCH, ex, ey, ez, 1, 0.2, 0.2, 0.2, 0.01);

                    if (amplifier >= 2) {
                        serverLevel.sendParticles(ParticleTypes.NOTE, ex, ey + 0.5, ez, 1, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}