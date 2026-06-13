package com.alteredstates.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public interface ISmokableItem {

    // Simula llevar el ítem a la boca (la animación del arco es perfecta para esto)
    default UseAnim getSmokeAnimation(ItemStack stack) {
        return UseAnim.BOW;
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
}