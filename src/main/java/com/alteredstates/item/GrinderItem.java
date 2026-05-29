package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
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
        super(properties.durability(128)); // Rompible/Reparable
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH; // Animación de frotado continuo
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30; // Tiempo total necesario para moler (1.5 segundos)
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack grinder = player.getItemInHand(hand);
        ItemStack offhandItem = player.getOffhandItem();

        // Solo empezamos a moler si tenemos cogollos secos en la mano izquierda
        if (hand == InteractionHand.MAIN_HAND && offhandItem.is(ModItems.INDICA_BUDS_DRY.get())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(grinder);
        }
        return InteractionResultHolder.pass(grinder);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide && entity instanceof Player player) {
            ItemStack offhandItem = player.getOffhandItem();

            // Sonido de molienda crujiente cada 4 ticks
            if (count % 4 == 0) {
                level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        com.alteredstates.registry.ModSounds.GRINDER_CRUSH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 0.8F, false);
            }

            // Partículas de cogollo saliendo disparadas de las manos
            if (!offhandItem.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, offhandItem),
                            player.getX() + (level.random.nextDouble() - 0.5D) * 0.4D,
                            player.getY() + 1.2D,
                            player.getZ() + (level.random.nextDouble() - 0.5D) * 0.4D,
                            0D, 0.1D, 0D);
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemStack offhandItem = player.getOffhandItem();

            if (offhandItem.is(ModItems.INDICA_BUDS_DRY.get())) {
                // Extraemos la calidad original del cogollo para heredarla
                int currentQuality = offhandItem.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                // Consumimos 1 cogollo
                if (!player.getAbilities().instabuild) {
                    offhandItem.shrink(1);
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                }

                // Creamos el output y le inyectamos la calidad exacta heredada
                ItemStack groundCannabis = new ItemStack(ModItems.CANNABIS_GROUND.get()); // Cambia al tuyo real
                groundCannabis.set(ModDataComponentTypes.QUALITY.get(), currentQuality);

                // Añadimos al inventario o dropeamos al suelo si está lleno
                if (!player.getInventory().add(groundCannabis)) {
                    player.drop(groundCannabis, false);
                }

                // Sonido de éxito final
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
        }
        return stack;
    }
}