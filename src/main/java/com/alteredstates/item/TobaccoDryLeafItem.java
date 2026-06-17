package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TobaccoDryLeafItem extends TobccoLeafItem implements ICurable, IGrindable {

    TobaccoLeafType type;

    public TobaccoDryLeafItem(Properties properties, TobaccoLeafType type) {
        super(properties, type);
    }


    @Override
    public Item getGrindResult(ItemStack stack) {
        // Al pasarlo por el grinder, nos devuelve la versión "Ground" correspondiente
        return ModItems.ROLLING_TOBACCO.get();
    }

    @Override
    public int getCuringTime(ItemStack stack) {
        return 6000; // Tiempo que tarda en el tarro en subir calidad (5m)
    }

}
