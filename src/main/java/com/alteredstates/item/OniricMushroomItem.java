package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
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

public class OniricMushroomItem extends Item implements IDryable {

    public OniricMushroomItem(Properties properties) {
        super(properties
                .component(ModDataComponentTypes.QUALITY.get(), 2)
                .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build())
        );
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Toxicidad cruda, da muchísimo sueño y te deja clavado en el sitio
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0)); // Mareo visual pesado
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));    // Ligeramente tóxica sin secar
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) this.applyProductEffects(entity, stack);
        return result;
    }

    @Override
    public Item getDriedResult(ItemStack stack) { return com.alteredstates.registry.ModItems.ONIRIC_MUSHROOM_DRIED.get(); }

    @Override
    public int getDryingTime(ItemStack stack) { return 1800; } // Tarda más en secar que la mística

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Quality qualityEnum = Quality.byLevel(stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1));
        tooltipComponents.add(qualityEnum.getTranslatedName());
        // Color rojo oscuro para reflejar su aspecto
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.oniric_mushroom_fresh").withStyle(net.minecraft.ChatFormatting.DARK_RED));
    }
}