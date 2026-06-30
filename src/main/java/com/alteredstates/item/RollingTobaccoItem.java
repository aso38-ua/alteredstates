package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RollingTobaccoItem extends Item implements IRollable, IBongable{
    public RollingTobaccoItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getRollResult(ItemStack stack) {
        return ModItems.CIGARETTE.get();
    }

    @Override
    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    @Override
    public String getContentType(ItemStack stack) {
        return "Rolling tobacco";
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return;

        int quality = getQuality(stack);

        // ⏱️ Duración: 600 ticks (30s) por nivel de calidad
        int duration = 600 / quality;

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.CONFUSION, duration, 0));

    }
}
