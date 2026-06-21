package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IProduct {

    // Método centralizado para leer calidad
    default int getQuality(ItemStack stack) {
        // Asumimos calidad NORMAL (2) por defecto si no tiene componente
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2);
    }

    // Método que implementaremos cuando hagamos los comestibles
    void applyProductEffects(LivingEntity entity, ItemStack stack);
}