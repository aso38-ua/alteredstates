package com.alteredstates.item;

import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Roca de cocaína: el grinder la convierte en polvo (conserva la calidad). */
public class CocaineRockItem extends Item implements IGrindable {

    public CocaineRockItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getGrindResult(ItemStack stack) {
        return ModItems.COCAINE_POWDER.get();
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
    }
}
