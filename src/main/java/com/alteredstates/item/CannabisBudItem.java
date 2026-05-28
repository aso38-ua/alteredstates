package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class CannabisBudItem extends Item {
    public CannabisBudItem(Properties properties) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int qualityLevel = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
        CannabisQuality quality = CannabisQuality.byLevel(qualityLevel);

        // Ahora usamos translatable en lugar de literal
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality")
                .append(quality.getTranslatedName()));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}