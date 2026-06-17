package com.alteredstates.item;

import com.alteredstates.item.ICurable;
import com.alteredstates.item.IGrindable;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DryBudItem extends CannabisBudItem implements ICurable, IGrindable {

    public DryBudItem(Properties properties, boolean isIndica) {
        super(properties, isIndica);
    }

    // --- Lógica del Tarro de Curado (ICurable) ---
    @Override
    public int getCuringTime(ItemStack stack) {
        return 6000; // Tiempo que tarda en el tarro en llegar a Premium
    }

    // --- Lógica del Grinder (IGrindable) ---
    @Override
    public Item getGrindResult(ItemStack stack) {
        // Al pasarlo por el grinder, nos devuelve la versión "Ground" correspondiente
        return getStrain(stack) == CannabisStrain.INDICA
                ? ModItems.INDICA_GROUND.get()
                : ModItems.SATIVA_GROUND.get();
    }
}