package com.alteredstates.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IGrindable extends IProduct {
    // Devuelve en qué se convierte al triturarse (ej: INDICA_BUDS_DRY -> INDICA_GROUND)
    Item getGrindResult(ItemStack stack);
}