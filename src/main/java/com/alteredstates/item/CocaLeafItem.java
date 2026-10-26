package com.alteredstates.item;

import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Hoja fresca de coca: se cuelga en la drying rack y sale hoja seca. */
public class CocaLeafItem extends Item implements IDryable {

    public CocaLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getDriedResult(ItemStack stack) {
        return ModItems.COCA_LEAF_DRY.get();
    }

    // Implementación de IProduct (la hoja no se consume directamente)
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
    }
}
