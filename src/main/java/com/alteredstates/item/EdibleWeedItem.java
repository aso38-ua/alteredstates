package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EdibleWeedItem extends Item implements IEdibleWeed {

    public EdibleWeedItem(Properties properties) {
        super(properties);
    }

    // Dentro de CannabutterItem.java
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
            boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);

            // ¡Usamos tu sistema de digestión!
            // Codificamos la cepa y la calidad en el Amplifier:
            // Índica: 0 a 9 (ej. Calidad 3 = Amp 3)
            // Sativa: 10 a 19 (ej. Calidad 3 = Amp 13)
            int hiddenCode = isIndica ? quality : (10 + quality);

            // Le damos el efecto de "Digestión" (Dura 30 segundos = 600 ticks)
            // Quitamos el .get() de ModEffects.DIGESTING
            player.addEffect(new MobEffectInstance(ModEffects.DIGESTING, 600, hiddenCode, false, false, true));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
        boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);

        String strainName = isIndica ? "Índica" : "Sativa";
        ChatFormatting strainColor = isIndica ? ChatFormatting.DARK_PURPLE : ChatFormatting.GREEN;

        // Añadimos la cepa
        tooltipComponents.add(Component.literal("Cepa: " + strainName).withStyle(strainColor));

        // 🟢 CORRECCIÓN AQUÍ: Enlazamos con tu enum de calidades
        tooltipComponents.add(Component.literal("Calidad: ").withStyle(ChatFormatting.GOLD)
                .append(Quality.byLevel(quality).getTranslatedName()));
    }
}