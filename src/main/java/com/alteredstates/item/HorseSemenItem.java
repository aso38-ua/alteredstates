package com.alteredstates.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.material.Fluid;

public class HorseSemenItem extends Item {
    public HorseSemenItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    // Animación de beber
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    // Tiempo para beber
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }


    // Inicia el uso del item (abre animación)
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        // Primero, intentamos usar el comportamiento nativo del cubo (colocar el líquido en el mundo)
        InteractionResultHolder<ItemStack> bucketResult = super.use(world, player, hand);

        // Si el cubo consumió la acción (es decir, apuntaba a un bloque y colocó el líquido)
        if (bucketResult.getResult().consumesAction()) {
            return bucketResult;
        }

        // Si falló (por ejemplo, el jugador está mirando al aire o lejos de un bloque), empezamos a beber
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    // Lógica al terminar de beber
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        ItemStack result = super.finishUsingItem(stack, world, entity);

        if (!world.isClientSide && entity instanceof Player player) {

            // Aplica efectos
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Integer.MAX_VALUE, 3));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 900, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 3));
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 900, 3));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 3));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 3));


            //player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Integer.MAX_VALUE, 3));
            //player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 900, 3));

            player.hurt(player.damageSources().starve(), 6.0F); // 1.0F = medio corazón


            // Si está en survival, deja bucket
            if (!player.isCreative()) {
                //player.setItemInHand(player.getUsedItemHand(), new ItemStack(Items.BUCKET));
                return new ItemStack(Items.BUCKET);
            }
        }

        // En creativo devuelve el mismo item, en survival el result normal
        if (entity instanceof Player p && p.isCreative()) {
            return stack;
        }
        return result;
    }
}