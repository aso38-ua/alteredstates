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

public class EdibleWeedItem extends Item {

    public EdibleWeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (!level.isClientSide() && entityLiving instanceof Player player) {

            // Leemos los datos del brownie
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
            boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);

            // Codificamos la cepa y la calidad en el "Amplificador" del efecto
            int encodedAmplifier = isIndica ? quality : (quality + 10);

            // EL RETRASO: 3600 ticks = 3 minutos reales hasta que haga efecto
            int delayTicks = 3600;

            // Aplicamos el estado de Digestión al jugador
            player.addEffect(new MobEffectInstance(ModEffects.DIGESTING, 3600, encodedAmplifier, false, false, true));
        }
        return super.finishUsingItem(stack, level, entityLiving);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
        boolean isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);

        String strainName = isIndica ? "Índica" : "Sativa";
        ChatFormatting strainColor = isIndica ? ChatFormatting.DARK_PURPLE : ChatFormatting.GREEN;

        // Añadimos la info visual
        tooltipComponents.add(Component.literal("Cepa: " + strainName).withStyle(strainColor));
        tooltipComponents.add(Component.literal("Calidad: " + quality).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("Tarda en subir, pero golpea fuerte...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}