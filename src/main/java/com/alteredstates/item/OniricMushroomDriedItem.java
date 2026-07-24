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

        // Si tienes un efecto ONIRICA creado, úsalo aquí.
        // Si no, simulamos el viaje sedante y "chill" con efectos vanilla.
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, amplifier)); // Te relaja los músculos
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, amplifier)); // No sientes el dolor
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks / 2, 0)); // Te cura pasivamente

        // Opcional: añadir tu efecto ModEffects.ONIRICA_EFFECT si existe.
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) this.applyProductEffects(entity, stack);
        return result;
    }

    @Override
    public int getCuringTime(ItemStack stack) { return 6000; }

    @Override
    public Item getGrindResult(ItemStack stack) { return com.alteredstates.registry.ModItems.ONIRIC_MUSHROOM_GROUND.get(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Quality qualityEnum = Quality.byLevel(stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1));
        tooltipComponents.add(qualityEnum.getTranslatedName());
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.oniric_mushroom_dried").withStyle(net.minecraft.ChatFormatting.RED));
    }
}