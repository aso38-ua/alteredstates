package com.alteredstates.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IDryable extends IProduct {

    // 1. ¿En qué se convierte cuando termina el proceso?
    // Ej: FreshBud -> DryBud
    Item getDriedResult(ItemStack stack);

    // 2. ¿Cuánto tarda en secarse?
    // Usamos 'default' para que, si no quieres complicarte,
    // tarde 24000 ticks (1 día de Minecraft) por defecto.
    default int getDryingTime(ItemStack stack) {
        return 24000;
    }
}
