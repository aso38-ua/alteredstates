package com.alteredstates.event;

import com.alteredstates.AlteredStates; // tu clase principal del modid
import com.alteredstates.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AlteredStates.MOD_ID)
public class HorseInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Horse horse
                && !horse.isBaby()
                && event.getItemStack().is(Items.BUCKET)
                && !event.getLevel().isClientSide) {

            ItemStack semenStack = new ItemStack(ModItems.HORSE_SEMEN.get());

            event.getLevel().playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                    SoundEvents.COW_MILK, SoundSource.NEUTRAL, 1.0F, 1.0F);

            ItemStack heldStack = event.getItemStack();
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                event.getEntity().setItemInHand(event.getHand(), semenStack);
            } else if (!event.getEntity().getInventory().add(semenStack)) {
                event.getEntity().drop(semenStack, false);
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}