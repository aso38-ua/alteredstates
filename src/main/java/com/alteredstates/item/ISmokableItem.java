package com.alteredstates.item;

import com.alteredstates.component.EmberState;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ISmokableItem {
    default SmokeAnimation getSmokeAnimation(ItemStack stack) {
        return SmokeAnimation.SMOKING;
    }

    default int getSmokeDuration(ItemStack stack) {
        return 32;
    }

    void playSmokingSound(Level level, LivingEntity entity, ItemStack stack);

    void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration);

    ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity);

    default InteractionResultHolder<ItemStack> startSmoking(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    // ------------------------------------------------------------------------------------------
    // Estado visual que leen los efectos de los huesos "paper" y "tip" (SmokeBoneEffects).
    // Todos tienen valor por defecto: un ítem solo sobrescribe lo que le aplica.
    // ------------------------------------------------------------------------------------------

    /** Parte que queda del papel: 1 = intacto, 0 = consumido. Sin consumo (pipas): 1. */
    default float burnProgress(ItemStack stack, @Nullable Level level) {
        return 1.0f;
    }

    /** Estado de la brasa (brillo, pulso, carbonizado). Lo guarda cualquier ítem en el componente EMBER. */
    default EmberState emberState(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
    }

    /** false = el hueso "tip" no se dibuja (p. ej. pipa vacía: no hay nada que arda en la cazoleta). */
    default boolean isTipVisible(ItemStack stack) {
        return true;
    }
}
