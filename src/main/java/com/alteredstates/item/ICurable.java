package com.alteredstates.item;

import net.minecraft.world.item.ItemStack;

public interface ICurable extends IProduct {
    // Cuánto tarda en alcanzar la calidad máxima (Premium)
    default int getCuringTime(ItemStack stack) {
        return 6000;
    }
}
