package com.alteredstates.item;

import net.minecraft.world.item.ItemStack;

public interface ICurable extends IProduct {
    // Cuánto tarda en alcanzar la calidad máxima (Premium)
    default int getCuringTime(ItemStack stack) {
        // para darle más realismo, o dejarlo simple devolviendo un número fijo.
        int quality = getQuality(stack);
        int dryingTime = 4000 + (quality * 600);
        if(quality<3){ dryingTime =+ 1200; } //La ultima calidad
        return dryingTime;
    }
}
