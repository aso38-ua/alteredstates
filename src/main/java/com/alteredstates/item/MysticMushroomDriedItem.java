package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MysticMushroomDriedItem extends Item implements ICurable, IGrindable {

    public MysticMushroomDriedItem(Properties properties) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 2));
    }

    // --- Implementación de IProduct ---
    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Por ahora vacío. Aquí irán los efectos cuando hagamos los comestibles/infusiones.
    }

    // --- Implementación de ICurable (El Tarro) ---
    @Override
    public int getCuringTime(ItemStack stack) {
        // 6000 ticks como el cannabis. Puedes ajustarlo si quieres que las setas tarden más o menos.
        return 6000;
    }

    // --- Implementación de IGrindable (El Grinder) ---
    @Override
    public Item getGrindResult(ItemStack stack) {
        // Al molerla, nos dará Seta Triturada (polvo de seta), ideal para hacer tés o bombones.
        return ModItems.MYSTIC_MUSHROOM_GROUND.get();
    }

    // --- Hover Text (Tooltip para ver la calidad) ---
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Leemos la calidad
        int qualityLevel = this.getQuality(stack);
        Quality qualityEnum = Quality.byLevel(qualityLevel);

        // Mostramos la calidad con su color
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality")
                .append(qualityEnum.getTranslatedName()));

        // Un subtítulo descriptivo
        //tooltipComponents.add(Component.translatable("tooltip.alteredstates.mystic_mushroom_dried").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}