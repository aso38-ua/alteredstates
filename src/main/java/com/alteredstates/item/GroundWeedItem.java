package com.alteredstates.item;

import com.alteredstates.item.IRollable;
import com.alteredstates.item.IBongable;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroundWeedItem extends CannabisBudItem implements IRollable, IBongable {

    public GroundWeedItem(Properties properties, boolean isIndica) {
        super(properties, isIndica);
    }

    @Override
    public Item getRollResult(ItemStack stack) {
        // Si es índica, devuelve el porro índica, si no, el sativa.
        // (Asumiendo que tienes un método getStrain() heredado)
        return getStrain(stack) == CannabisStrain.INDICA ? ModItems.INDICA_JOINT.get() : ModItems.SATIVA_JOINT.get();
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Aquí pones la lógica de los efectos potentes y rápidos de fumar en bong
    }
}