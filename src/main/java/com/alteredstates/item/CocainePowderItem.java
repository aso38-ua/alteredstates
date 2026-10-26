package com.alteredstates.item;

import com.alteredstates.cocaine.CocaineEffects;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;

/**
 * Polvo de cocaína. Se esnifa: brazo levantado hacia la cara (animación TOOT_HORN),
 * sonidos de sniffer y partículas de polvo junto a la nariz.
 */
public class CocainePowderItem extends Item implements IProduct {
    private static final int SNIFF_TICKS = 32;

    public CocainePowderItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return SNIFF_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNIFFER_SNIFFING, SoundSource.PLAYERS, 0.9F, 1.3F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide && remaining % 3 == 0) {
            Vec3 look = entity.getLookAngle();
            Vec3 face = entity.getEyePosition().add(look.scale(0.35D)).add(0, -0.12D, 0);
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                    face.x, face.y, face.z,
                    (level.random.nextDouble() - 0.5D) * 0.02D, 0.02D, (level.random.nextDouble() - 0.5D) * 0.02D);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2);
            CocaineEffects.use(player, quality);
            if (!player.hasInfiniteMaterials()) stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
    }
}
