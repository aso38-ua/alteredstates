package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class GrinderItem extends Item {

    public GrinderItem(Properties properties) {
        super(properties.durability(128));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack thisItem = player.getItemInHand(hand);
        ItemStack otherItem = player.getItemInHand(
                hand == InteractionHand.MAIN_HAND
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND
        );

        // Grinder en ESTA mano, material en la otra
        if (thisItem.getItem() instanceof GrinderItem
                && otherItem.getItem() instanceof IGrindable) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(thisItem);
        }

        // Material en ESTA mano, grinder en la otra
        // (el jugador usa el item grindable y tiene el grinder en la otra mano)
        if (thisItem.getItem() instanceof IGrindable
                && otherItem.getItem() instanceof GrinderItem) {
            // Delegamos al grinder de la otra mano
            player.startUsingItem(
                    hand == InteractionHand.MAIN_HAND
                            ? InteractionHand.OFF_HAND
                            : InteractionHand.MAIN_HAND
            );
            return InteractionResultHolder.consume(thisItem);
        }

        return InteractionResultHolder.pass(thisItem);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide && entity instanceof Player player) {
            // Buscamos el material grindable en cualquier mano
            ItemStack material = getGrindableMaterial(player);

            if (count % 4 == 0) {
                level.playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        com.alteredstates.registry.ModSounds.GRINDER_CRUSH.get(),
                        SoundSource.PLAYERS, 0.6F, 0.8F, false
                );
            }

            if (!material.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(
                            new ItemParticleOption(ParticleTypes.ITEM, material),
                            player.getX() + (level.random.nextDouble() - 0.5D) * 0.4D,
                            player.getY() + 1.2D,
                            player.getZ() + (level.random.nextDouble() - 0.5D) * 0.4D,
                            0D, 0.1D, 0D
                    );
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemStack material = getGrindableMaterial(player);
            ItemStack grinderStack = getGrinderStack(player);

            if (!material.isEmpty() && material.getItem() instanceof IGrindable grindable) {
                int quality = material.getOrDefault(
                        ModDataComponentTypes.QUALITY.get(), 1);

                if (!player.getAbilities().instabuild) {
                    material.shrink(1);
                    // Dañamos el grinder que tenga en cualquier mano
                    if (grinderStack != null) {
                        grinderStack.hurtAndBreak(1, player,
                                LivingEntity.getSlotForHand(getGrinderHand(player)));
                    }
                }

                Item groundResult = grindable.getGrindResult(material);
                ItemStack groundStack = new ItemStack(groundResult);
                groundStack.set(ModDataComponentTypes.QUALITY.get(), quality);

                if (!player.getInventory().add(groundStack)) {
                    player.drop(groundStack, false);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
        }
        return stack;
    }

    // ── Helpers ───────────────────────────────────────────────────

    /** Devuelve el ItemStack grindable del jugador, esté en la mano que esté */
    private static ItemStack getGrindableMaterial(Player player) {
        if (player.getMainHandItem().getItem() instanceof IGrindable)
            return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof IGrindable)
            return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    /** Devuelve el ItemStack del grinder del jugador */
    private static ItemStack getGrinderStack(Player player) {
        if (player.getMainHandItem().getItem() instanceof GrinderItem)
            return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof GrinderItem)
            return player.getOffhandItem();
        return null;
    }

    /** Devuelve la mano que tiene el grinder */
    private static InteractionHand getGrinderHand(Player player) {
        return player.getMainHandItem().getItem() instanceof GrinderItem
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
    }
}