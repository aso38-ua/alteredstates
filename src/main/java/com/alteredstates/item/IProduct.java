package com.alteredstates.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IProduct {
    int getQuality(ItemStack stack);
    void applyProductEffects(LivingEntity entity, ItemStack stack);
}