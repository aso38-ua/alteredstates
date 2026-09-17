package com.alteredstates.item;

import net.minecraft.world.item.ItemStack;

public interface ICannabisProduct extends IProduct {
    CannabisStrain getStrain(ItemStack stack);
}
