package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// Solo necesitamos implementar IDryable, porque IDryable ya extiende IProduct
public class MysticMushroomItem extends Item implements IDryable {

    public MysticMushroomItem(Properties properties) {
        // Calidad 2 (Normal) por defecto
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 2));
    }

    // --- Implementación de IProduct ---
    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Por ahora no hace nada. Esto será para cuando hagamos que se pueda comer.
    }

    // --- Implementación de IDryable ---
    @Override
    public Item getDriedResult(ItemStack stack) {
        // En tu interfaz, devuelves el "Item", no un "ItemStack".
        // OJO: Esto significa que la transferencia de la CALIDAD de la seta fresca
        // a la seta seca tendrás que programarla en la clase del Bloque del Tendedero.
        return ModItems.MYSTIC_MUSHROOM_DRIED.get();
    }

    @Override
    public int getDryingTime(ItemStack stack) {
        // Sobrescribimos el default de 24000 ticks para que tarde solo 1 minuto (1200)
        // Puedes borrar este método entero si prefieres que tarde 1 día de Minecraft.
        return 400;
    }

    // --- Hover Text (Tooltip) ---
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Leemos calidad usando el método heredado de IProduct
        Quality qualityEnum = Quality.byLevel(this.getQuality(stack));

        // Mostramos la calidad con su color
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality")
                .append(qualityEnum.getTranslatedName()));

        //tooltipComponents.add(Component.translatable("tooltip.alteredstates.mystic_mushroom_fresh").withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
    }
}