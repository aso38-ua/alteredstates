package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public class TobaccoFreshLeafItem extends TobccoLeafItem implements IDryable{

    TobaccoLeafType type;

    public TobaccoFreshLeafItem(Properties properties, TobaccoLeafType type){
        super(properties, type);
        this.type = type;
    }

    @Override
    public Item getDriedResult(ItemStack stack) {
        switch (getType(stack)){
            case CAPA -> {
                return ModItems.CAPA_DRY.get();
            }
            case CAPOTE -> {
                return ModItems.CAPOTE_DRY.get();
            }
            default -> {
                return ModItems.TRIPA_DRY.get();
            }
        }
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {

    }

    @Override
    public int getDryingTime(ItemStack stack) {
        // para darle más realismo, o dejarlo simple devolviendo un número fijo.
        int quality = getQuality(stack);
        int dryingTime = 3000 + (quality * 600);
        if(quality<2){ dryingTime =+ 1000; } //La ultima calidad o las ultimas duran mas
        return dryingTime;
    }
}
