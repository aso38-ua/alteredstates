package com.alteredstates.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IRollable extends IProduct {
    Item getRollResult(ItemStack stack); // Devuelve el porro/cigarro resultante
    // Devuelve la calidad (esto ya lo tienes)
    int getQuality(ItemStack stack);

    // NUEVO: Devuelve un identificador de lo que lleva dentro (ej: "mystic_mushroom")
    String getContentType(ItemStack stack);
}
