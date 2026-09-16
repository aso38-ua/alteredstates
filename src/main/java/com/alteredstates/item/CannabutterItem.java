package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CannabutterItem extends Item implements IEdibleWeed {

    public CannabutterItem(Properties properties) {
        // Establecemos el componente de calidad por defecto en 1
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1));
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
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {

        // Leemos los datos
        boolean isIndica = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.IS_INDICA.get(), true);
        int qualityLevel = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), 1);

        // Formateamos la Cepa
        String strainName = isIndica ? "Índica" : "Sativa";
        net.minecraft.ChatFormatting strainColor = isIndica ? net.minecraft.ChatFormatting.DARK_PURPLE : net.minecraft.ChatFormatting.GREEN;

        // Extraemos la calidad usando tu ENUM
        Quality qualityEnum = Quality.byLevel(qualityLevel);

        // Añadimos el texto al tooltip
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("Cepa: " + strainName).withStyle(strainColor));

        // Usamos el nombre traducido y su color automático (ej. "PREMIUM" en dorado)
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("Calidad: ").append(qualityEnum.getTranslatedName()));
    }
}
