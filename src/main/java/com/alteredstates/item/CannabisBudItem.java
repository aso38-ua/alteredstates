package com.alteredstates.item;

import com.alteredstates.item.CannabisStrain;
import com.alteredstates.item.ICannabisProduct;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class CannabisBudItem extends Item implements ICannabisProduct {

    private final boolean isIndica;

    public CannabisBudItem(Properties properties, boolean isIndica) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.IS_INDICA.get(), isIndica));
        this.isIndica = isIndica;
    }

    // Implementación de la interfaz IProduct
    @Override
    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {

    }

    // Implementación de la interfaz ICannabisProduct
    @Override
    public CannabisStrain getStrain(ItemStack stack) {
        // Leemos el componente, si no existe, usamos el valor del constructor
        boolean indica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), this.isIndica);
        return CannabisStrain.fromBoolean(indica);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Usamos los métodos de la interfaz en lugar de acceder directamente al componente
        int qualityLevel = getQuality(stack);
        CannabisStrain strain = getStrain(stack);

        CannabisQuality quality = CannabisQuality.byLevel(qualityLevel);

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.strain")
                .append(Component.literal(": " + strain.name()))); // Ajusta esto a tu estilo

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality")
                .append(quality.getTranslatedName()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}