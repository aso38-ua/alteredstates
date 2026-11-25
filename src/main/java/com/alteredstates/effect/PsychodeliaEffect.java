package com.alteredstates.effect;

import com.alteredstates.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import com.alteredstates.util.EffectUtil;

import java.util.List;

public class PsychodeliaEffect extends MobEffect {

    public PsychodeliaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player) || !(player.level() instanceof ServerLevel serverLevel)) {
            return true;
        }
        MobEffectInstance current = player.getEffect(ModEffects.PSYCHODELIA);
        if (current == null) {
            return true;
        }
        int duration = current.getDuration();

        if (duration <= 100) {
            player.removeEffect(MobEffects.SLOW_FALLING);
            EffectUtil.refresh(player, MobEffects.LEVITATION, 10, 2, 5);
            // antes 2 partículas/tick; ahora 10 cada 5 ticks (misma densidad)
            serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY(), player.getZ(),
                    10, 0.2, 0.2, 0.2, 0.05);
            return true;
        }

        EffectUtil.refresh(player, MobEffects.SLOW_FALLING, 20, 0, 10);
        EffectUtil.refresh(player, MobEffects.JUMP, 20, amplifier, 10);
        EffectUtil.refresh(player, MobEffects.DIG_SPEED, 20, amplifier, 10);

        // antes: tickCount % 30. Ahora basado en duration (múltiplo de 5 garantizado)
        if (amplifier >= 4 && player.getHealth() < player.getMaxHealth() && duration % 30 == 0) {
            player.heal(1.0f);
        }

        // antes 0.08 por tick; ahora 0.4 cada 5 ticks (misma esperanza)
        if (player.getRandom().nextFloat() < 0.4f && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
            double wobbleX = (player.getRandom().nextFloat() - 0.5) * 0.22;
            double wobbleZ = (player.getRandom().nextFloat() - 0.5) * 0.22;
            player.setDeltaMovement(player.getDeltaMovement().add(wobbleX, 0.0, wobbleZ));
            player.hurtMarked = true;
        }

        if (duration % 20 == 0) {
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
        return true;
    }
}