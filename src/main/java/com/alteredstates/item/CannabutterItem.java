package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CannabutterItem extends Item {

    public CannabutterItem(Properties properties) {
        // Establecemos el componente de calidad por defecto en 1
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            // 1. Leemos tanto la calidad como la cepa de la mantequilla
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
            boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);

            // 2. Asignamos el efecto base de forma dinámica (Índica o Sativa)
            var mainEffect = isIndica ? ModEffects.INDICA_EFFECT : ModEffects.SATIVA_EFFECT;

            // 3. Aplicamos los efectos correspondientes según la calidad
            if (quality >= 3) {
                // Calidad Buena/Premium: Colocón larguísimo y limpio (¡Sin paranoia porque es del bueno!)
                player.addEffect(new MobEffectInstance(mainEffect, 1200, 1)); // 60s, Nivel 2

            } else if (quality == 2) {
                // Calidad Media (Normal): Colocón estándar, muy agradable
                player.addEffect(new MobEffectInstance(mainEffect, 600, 0));  // 30s, Nivel 1

            } else {
                // Calidad Baja (Regular/Basura): Efecto muy corto y suave...
                player.addEffect(new MobEffectInstance(mainEffect, 200, 0));  // 10s, Nivel 1

                // 🎲 ¡PROBABILIDAD DE MAL VIAJE!: Si la calidad es mala, hay un 60% de probabilidad de paranoia
                if (level.random.nextFloat() < 0.60f) {
                    player.addEffect(new MobEffectInstance(ModEffects.PARANOIA, 300, 0)); // 15s de paranoia
                }
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {

        // Leemos los datos
        boolean isIndica = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.IS_INDICA.get(), true);
        int qualityLevel = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), 1);

        // Formateamos la Cepa
        String strainName = isIndica ? "Índica" : "Sativa";
        net.minecraft.ChatFormatting strainColor = isIndica ? net.minecraft.ChatFormatting.DARK_PURPLE : net.minecraft.ChatFormatting.GREEN;

        // Extraemos la calidad usando tu ENUM
        com.alteredstates.item.CannabisQuality qualityEnum = com.alteredstates.item.CannabisQuality.byLevel(qualityLevel);

        // Añadimos el texto al tooltip
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("Cepa: " + strainName).withStyle(strainColor));

        // Usamos el nombre traducido y su color automático (ej. "PREMIUM" en dorado)
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("Calidad: ").append(qualityEnum.getTranslatedName()));
    }
}
