package com.alteredstates.item;

import com.alteredstates.component.EmberState;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ISmokableItem {
    default SmokeAnimation getSmokeAnimation(ItemStack stack) {
        return SmokeAnimation.SMOKING;
    }

    // Cuánto dura una calada (32 ticks = 1.6 segundos)
    default int getSmokeDuration(ItemStack stack) {
        return 32;
    }

    // Efecto de sonido al empezar a fumar o durante la calada
    void playSmokingSound(Level level, LivingEntity entity, ItemStack stack);

    // Partículas de humo (se ejecuta cada tick mientras mantienes el clic)
    void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration);

    // Qué pasa cuando terminas la calada (restar durabilidad, aplicar efectos)
    ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity);

    // Lógica por defecto para activar el fumado al hacer clic derecho
    default InteractionResultHolder<ItemStack> startSmoking(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    /**
     * Versión mejorada de startSmoking que detecta mechero en cualquier mano.
     * El item fumable puede estar en cualquiera de las dos manos,
     * y el mechero en la otra.
     */
    default InteractionResultHolder<ItemStack> startSmokingAnyHand(
            Level level, Player player, InteractionHand hand) {

        ItemStack thisItem  = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherItem = player.getItemInHand(otherHand);

        boolean lighterInThisHand  = isLighter(thisItem);
        boolean lighterInOtherHand = isLighter(otherItem);

        // Caso 1: este item es fumable, mechero en la otra mano
        if (!lighterInThisHand && lighterInOtherHand
                && thisItem.getItem() instanceof ISmokableItem) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(thisItem);
        }

        // Caso 2: este item es el mechero, fumable en la otra mano
        if (lighterInThisHand
                && otherItem.getItem() instanceof ISmokableItem) {
            player.startUsingItem(otherHand);
            return InteractionResultHolder.consume(otherItem);
        }

        return InteractionResultHolder.pass(thisItem);
    }

    private static boolean isLighter(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.FLINT_AND_STEEL);
    }

    default boolean isLoaded(ItemStack stack) {
        return true;
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