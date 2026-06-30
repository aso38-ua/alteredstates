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

public class MysticMushroomDriedItem extends Item implements ICurable, IGrindable {

    public MysticMushroomDriedItem(Properties properties) {
        super(properties
                .component(ModDataComponentTypes.QUALITY.get(), 2)
                .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).alwaysEdible().build())
        );
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        int quality = this.getQuality(stack);

        // Viaje Extendido: 60 segundos por nivel de calidad
        // Calidad 2 (Normal) = 2 min | Calidad 3 (Buena) = 3 min | Calidad 4 (Premium) = 4 min
        int durationTicks = (quality * 60) * 20;

        // Escalado de Intensidad Física:
        // Calidad 2 -> Amp 0 (Salto I, Haste I)
        // Calidad 3 -> Amp 1 (Salto II, Haste II)
        // Calidad 4 -> Amp 2 (Premium: Salto III, Haste III + Regeneración mística en el efecto)
        int amplifier = Math.max(0, quality - 2);

        // Aplicamos el efecto psicodélico limpio con su Holder directo
        entity.addEffect(new MobEffectInstance(ModEffects.PSYCHODELIA, durationTicks, amplifier));

        // Un pequeño mareo de pantalla al principio para simular la subida (10 segundos)
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
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
    public int getCuringTime(ItemStack stack) { return 6000; }

    @Override
    public Item getGrindResult(ItemStack stack) { return com.alteredstates.registry.ModItems.MYSTIC_MUSHROOM_GROUND.get(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Quality qualityEnum = Quality.byLevel(this.getQuality(stack));
        tooltipComponents.add(qualityEnum.getTranslatedName());
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.mystic_mushroom_dried").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}