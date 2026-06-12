package com.alteredstates.event;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModEffects;
import com.alteredstates.util.SmokingEffectProcessor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = AlteredStates.MOD_ID)
public class EdibleEffectHandler {

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().is(ModEffects.DIGESTING)) {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

                // Leemos el número secreto
                int amplifier = event.getEffectInstance().getAmplifier();

                // Decodificamos (Si es menor de 10, es Índica. Si es mayor, es Sativa).
                boolean isIndica = amplifier < 10;
                int quality = isIndica ? amplifier : (amplifier - 10);

                // IMPORTANTE: Asegúrate de llamar a 'applyEdibleEffects' y no a 'applyBongEffects'
                com.alteredstates.util.SmokingEffectProcessor.applyEdibleEffects(player, isIndica, quality);
            }
        }
    }


}