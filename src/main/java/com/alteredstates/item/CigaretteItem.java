package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.Vec3;

public class CigaretteItem extends Item implements ISmokableItem {


    public CigaretteItem(Properties properties) {
        super(properties);
    }

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {

    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {

    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        return null;
    }
}