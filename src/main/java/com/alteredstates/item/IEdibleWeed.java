package com.alteredstates.item;

import com.alteredstates.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public interface IEdibleWeed {
    // Método que fuerza a cualquier comestible a tener esta lógica de efectos
    default void applyHighEffect(Player player, int quality, boolean isIndica) {
        int encodedAmplifier = isIndica ? quality : (quality + 10);

        // 3600 ticks = 3 minutos de retraso para el efecto final
        player.addEffect(new MobEffectInstance(ModEffects.DIGESTING, 3600, encodedAmplifier, false, false, true));
    }
}
