package com.alteredstates.item;

import com.alteredstates.item.IRollable;
import com.alteredstates.item.IBongable;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroundWeedItem extends CannabisBudItem implements IRollable, IBongable, IPipable {

    public GroundWeedItem(Properties properties, boolean isIndica) {
        super(properties, isIndica);
    }

    @Override
    public Item getRollResult(ItemStack stack) {
        // Sea sativa o índica, la bandeja ahora siempre escupe nuestro porro genérico
        return ModItems.ROLLED_JOINT.get();
    }

    // 🚀 ESTO ES LO QUE FALTABA: El "chivato" que le dice al porro qué lleva dentro
    @Override
    public String getContentType(ItemStack stack) {
        return getStrain(stack) == CannabisStrain.INDICA ? "indica" : "sativa";
    }

    @Override
    public void applyProductEffects(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return;

        int quality = getQuality(stack);
        CannabisStrain strain = getStrain(stack);

        // ⏱️ Duración: 600 ticks (30s) por nivel de calidad
        int duration = 600 * quality;
        int amplifier = quality > 2 ? 1 : 0; // Efectos nivel 2 si es calidad 3 o 4

        // 1️⃣ APLICAR LOS EFECTOS PERSONALIZADOS
        if (strain == CannabisStrain.INDICA) {
            // Índica
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.INDICA_EFFECT, duration, amplifier));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, duration / 2, 0));
        } else if (strain == CannabisStrain.SATIVA) {
            // Sativa
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.SATIVA_EFFECT, duration, amplifier));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, duration, amplifier));
        }

        // 2️⃣ SISTEMA DE PARANOIA (Mal Viaje)
        float paranoiaChance = 0.50f - (quality * 0.10f);

        if (player.getRandom().nextFloat() < paranoiaChance) {
            // Paranoia
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.PARANOIA, duration, 0));
        }
    }
}