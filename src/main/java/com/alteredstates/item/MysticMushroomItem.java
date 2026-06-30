package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MysticMushroomItem extends Item implements IDryable {

    public MysticMushroomItem(Properties properties) {
        super(properties
                .component(ModDataComponentTypes.QUALITY.get(), 2) // Calidad Normal por defecto
                .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build())
        );
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        int quality = this.getQuality(stack);

        // Viaje Corto: 15 segundos por nivel de calidad (Calidad 2 = 30 segundos)
        int durationTicks = (quality * 15) * 20;

        // Al ser fresca el viaje físico siempre es de baja intensidad (Amplificador 0)
        entity.addEffect(new MobEffectInstance(ModEffects.PSYCHODELIA, durationTicks, 0));

        // EFECTOS SECUNDARIOS TOXICIDAD CRUDA
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0)); // Mareo visual
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 0));    // Malestar de tripa
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            this.applyProductEffects(entity, stack);
        }
        return result;
    }

    @Override
    public Item getDriedResult(ItemStack stack) { return com.alteredstates.registry.ModItems.MYSTIC_MUSHROOM_DRIED.get(); }

    @Override
    public int getDryingTime(ItemStack stack) { return 1200; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Quality qualityEnum = Quality.byLevel(this.getQuality(stack));
        tooltipComponents.add(qualityEnum.getTranslatedName());
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.mystic_mushroom_fresh").withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
    }
}