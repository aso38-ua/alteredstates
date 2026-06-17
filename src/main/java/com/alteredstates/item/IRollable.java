package com.alteredstates.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IRollable extends IProduct {
    Item getRollResult(ItemStack stack); // Devuelve el porro/cigarro resultante
}
