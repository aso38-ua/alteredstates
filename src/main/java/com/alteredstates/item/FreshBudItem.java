package com.alteredstates.item;

import com.alteredstates.item.IDryable;
import com.alteredstates.registry.ModItems;
import com.alteredstates.item.CannabisStrain;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FreshBudItem extends CannabisBudItem implements IDryable {

    public FreshBudItem(Properties properties, boolean isIndica) {
        super(properties, isIndica);
    }

    @Override
    public Item getDriedResult(ItemStack stack) {
        // Devuelve el cogollo seco correspondiente a su cepa
        return getStrain(stack) == CannabisStrain.INDICA
                ? ModItems.INDICA_BUDS_DRY.get()
                : ModItems.SATIVA_BUDS_DRY.get();
    }

    @Override
    public int getDryingTime(ItemStack stack) {
        // para darle más realismo, o dejarlo simple devolviendo un número fijo.
        int quality = getQuality(stack);
        return 6000 + (quality * 1200);
    }
}