package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class TobccoLeafItem extends Item implements ITobaccoProduct {

    private final TobaccoLeafType type;

    public TobccoLeafItem(Properties properties, TobaccoLeafType type) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1).component(ModDataComponentTypes.TOBACCO_LEAF_TYPE.get(), type));
        this.type = type;
    }

    // Implementación de la interfaz IProduct
    @Override
    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {

    }

    public TobaccoLeafType getType(ItemStack stack) {
        // Leemos el componente, si no existe, usamos el valor del constructor
        return stack.getOrDefault(ModDataComponentTypes.TOBACCO_LEAF_TYPE.get(), this.type);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Usamos los métodos de la interfaz en lugar de acceder directamente al componente
        int qualityLevel = getQuality(stack);

        Quality quality = Quality.byLevel(qualityLevel);

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.strain")
                .append(Component.literal(": " + type.getSerializedName()))); // Ajusta esto a tu estilo

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality")
                .append(quality.getTranslatedName()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}