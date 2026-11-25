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
import com.alteredstates.util.EffectUtil;

public class OniricTranceEffect extends MobEffect {

    public OniricTranceEffect(MobEffectCategory category, int color) {
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
        MobEffectInstance current = player.getEffect(ModEffects.ONIRIC_TRANCE);
        if (current == null) {
            return true;
        }
        int duration = current.getDuration();

        EffectUtil.refresh(player, MobEffects.DAMAGE_RESISTANCE, 55, amplifier + 1, 35);
        EffectUtil.refresh(player, MobEffects.BLINDNESS, 220, 0, 200);
        EffectUtil.refresh(player, MobEffects.MOVEMENT_SLOWDOWN, 55, Math.min(amplifier, 2), 35);
        EffectUtil.refresh(player, MobEffects.SLOW_FALLING, 55, 0, 35);
        EffectUtil.refresh(player, MobEffects.REGENERATION, 25, 0, 10);

        // antes: tickCount % 40
        if (duration % 40 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0f);
        }

        if (duration % 10 == 0) {
            double px = player.getX();
            double py = player.getY() + 1.0;
            double pz = player.getZ();
            serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 4 + amplifier * 2, 0.4, 0.5, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, px, py, pz, 3, 0.3, 0.4, 0.3, 0.02);
        }
        return true;
    }
}
