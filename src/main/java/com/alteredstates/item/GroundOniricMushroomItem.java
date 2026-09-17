package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GroundOniricMushroomItem extends Item implements IRollable, IBongable {

    public GroundOniricMushroomItem(Properties properties) {
        super(properties);
    }

    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    @Override
    public Item getRollResult(ItemStack stack) {
        return ModItems.ROLLED_JOINT.get();
    }

    @Override
    public String getContentType(ItemStack stack) {
        return "oniric_mushroom"; // 🍄 El chivato onírico
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (!(entity instanceof Player player)) return;

        int quality = getQuality(stack);
        int amplifier = quality >= 3 ? 1 : 0;
        int duration = 200 * quality;

        // Efecto Bong "Chill"
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier));
    }
}