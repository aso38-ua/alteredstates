package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EdibleWeedItem extends Item {

    public EdibleWeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        ItemStack result = super.finishUsingItem(stack, level, entityLiving);

        if (!level.isClientSide && entityLiving instanceof Player player) {
            boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            // ⏳ TIEMPO DE RETARDO: p.ej. 30 segundos (600 ticks) de digestión antes de que suba
            int delayTicks = 600;

            // 💾 CODIFICACIÓN: Guardamos los datos en el nivel del efecto (amplifier)
            // Si es Indica: de 1 a 3. Si es Sativa: de 11 a 13.
            int encodedAmplifier = (isIndica ? 0 : 10) + quality;

            // Aplicamos el efecto de digestión
            player.addEffect(new MobEffectInstance(ModEffects.DIGESTING, delayTicks, encodedAmplifier, false, false, true));
        }

        return result;
    }
}