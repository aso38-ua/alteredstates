package com.alteredstates.item;

import net.minecraft.world.item.ItemStack;

public interface ICurable extends IProduct {
    // Cuánto tarda en alcanzar el siguiente nivel de calidad en el tarro de curado
    default int getCuringTime(ItemStack stack) {
        int quality = getQuality(stack);
        int baseTime = com.alteredstates.Config.CURING_TIME.get();
        int qualityAdd = (quality * 600);
        if (quality < 3) {
            qualityAdd += 1200;
        }
        return baseTime + qualityAdd;
    }
}
