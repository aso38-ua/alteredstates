package com.alteredstates.item;

import com.alteredstates.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TobaccoDryLeafItem extends TobccoLeafItem implements ICurable, IGrindable {

    public TobaccoDryLeafItem(Properties properties, TobaccoLeafType type) {
        super(properties, type);
    }

    @Override
    public Item getGrindResult(ItemStack stack) {
        // Al pasarlo por el grinder, nos devuelve la versión "Ground" correspondiente
        return ModItems.ROLLING_TOBACCO.get();
    }


}

