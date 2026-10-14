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

public class OniricMushroomDriedItem extends Item implements ICurable, IGrindable {

    public OniricMushroomDriedItem(Properties properties) {
        super(properties
                .component(ModDataComponentTypes.QUALITY.get(), 2)
                .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible().build())
        );
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
        int durationTicks = (quality * 60) * 20;
        int amplifier = Math.max(0, quality - 2);

        entity.addEffect(new MobEffectInstance(ModEffects.ONIRIC_TRANCE, durationTicks, amplifier));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) this.applyProductEffects(entity, stack);
        return result;
    }



    @Override
    public Item getGrindResult(ItemStack stack) { return com.alteredstates.registry.ModItems.ONIRIC_MUSHROOM_GROUND.get(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Quality qualityEnum = Quality.byLevel(stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1));
        //tooltipComponents.add(qualityEnum.getTranslatedName());
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.oniric_mushroom_dried").withStyle(net.minecraft.ChatFormatting.RED));
    }
}