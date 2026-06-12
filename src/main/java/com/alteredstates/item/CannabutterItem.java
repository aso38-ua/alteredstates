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
            // Leemos la calidad dinámica (por defecto 1 si no tiene)
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            // 🌿 Aplicamos los efectos correspondientes según la calidad
            // En la versión 1.21, los constructores de MobEffectInstance reciben un Holder<MobEffect>.
            // Dado que ModEffects.INDICA_EFFECT y ModEffects.PARANOIA son de tipo DeferredHolder (que implementa Holder),
            // se pueden pasar directamente sin invocar .get().
            if (quality >= 3) {
                // Calidad Premium: Colocón larguísimo y potente, pero con un toque de paranoia inicial por la potencia
                player.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, 1200, 1)); // 60s (1200 ticks), Nivel 2 (amplificador 1)
                player.addEffect(new MobEffectInstance(ModEffects.PARANOIA, 200, 0));      // 10s (200 ticks) de paranoia
            } else if (quality == 2) {
                // Calidad Media: Colocón estándar, muy agradable
                player.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, 600, 0));  // 30s (600 ticks), Nivel 1 (amplificador 0)
            } else {
                // Calidad Baja (Regular) o menor: Efecto muy corto y suave
                player.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, 200, 0));  // 10s (200 ticks), Nivel 1 (amplificador 0)
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int qualityLevel = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
        CannabisQuality quality = CannabisQuality.byLevel(qualityLevel);

        // Agregamos el texto "⭐ Calidad: " seguido del nombre formateado/traducido de la calidad
        tooltipComponents.add(Component.literal("⭐ Calidad: ").append(quality.getTranslatedName()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
