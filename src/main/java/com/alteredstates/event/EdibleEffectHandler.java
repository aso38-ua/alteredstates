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
        // 🔄 CORREGIDO: Usamos .is(ModEffects.DIGESTING) para comparar los Holders correctamente en 1.21
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().is(ModEffects.DIGESTING)) {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

                int amplifier = event.getEffectInstance().getAmplifier();

                // DECODIFICACIÓN
                boolean isIndica = amplifier < 10;
                int quality = isIndica ? amplifier : (amplifier - 10);

                // EL SUBIDÓN
                com.alteredstates.util.SmokingEffectProcessor.applyBongEffects(player, isIndica, quality);
            }
        }
    }


}