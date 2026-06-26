package com.alteredstates.item;

import net.minecraft.world.item.ItemStack;

public interface ICurable extends IProduct {
    // Cuánto tarda en alcanzar la calidad máxima (Premium)
    default int getCuringTime(ItemStack stack) {
        // para darle más realismo, o dejarlo simple devolviendo un número fijo.
        int quality = getQuality(stack);
        int dryingTime = 3000 + (quality * 600);
        if(quality<2){ dryingTime =+ 1000; } //La ultima calidad o las ultimas duran mas
        return dryingTime;
    }
}
