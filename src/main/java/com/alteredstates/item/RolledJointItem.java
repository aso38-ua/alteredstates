package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RolledJointItem extends Item implements ISmokableItem {

    public RolledJointItem(Properties properties) {
        super(properties.stacksTo(1).durability(4)
                .component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.CONTENT_TYPE.get(), "unknown"));
    }

    // ─── LECTORES DE DATOS ──────────────────────────────────────────────────

    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    public String getContentType(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.CONTENT_TYPE.get(), "unknown");
    }

    // ─── LÓGICA DEL SEMÁFORO (EFECTOS) ──────────────────────────────────────

    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) return;

        int quality = getQuality(stack);
        String content = getContentType(stack);
        int amplifier = quality >= 3 ? 1 : 0;

        switch (content) {
            case "indica":
                entity.addEffect(new MobEffectInstance(ModEffects.INDICA_EFFECT, 800 * quality, amplifier));
                if (quality >= 3) entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                applyParanoia(entity, quality);
                break;

            case "sativa":
                entity.addEffect(new MobEffectInstance(ModEffects.SATIVA_EFFECT, 800 * quality, amplifier));
                if (quality >= 3) entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                applyParanoia(entity, quality);
                break;

            case "mystic_mushroom":
                entity.addEffect(new MobEffectInstance(ModEffects.PSYCHODELIA, 200 * quality, amplifier));
                break;

            default:
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                break;
        }
    }

    private void applyParanoia(LivingEntity entity, int quality) {
        float paranoiaChance = quality <= 1 ? 0.20f : 0.05f;
        if (entity.level().random.nextFloat() < paranoiaChance) {
            int badDuration = 600 + (100 * quality);
            entity.addEffect(new MobEffectInstance(ModEffects.PARANOIA, badDuration, 0));
        }
    }

    // ─── LÓGICA DE JUEGO NATURA (DELEGANDO A LA INTERFAZ) ───────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        playSmokingSound(level, player, player.getItemInHand(hand));
        return this.startSmoking(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.getSmokeAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.getSmokeDuration(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        this.spawnSmokeParticles(level, entity, stack, remainingUseDuration);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return this.onSmokeFinished(stack, level, entity);
    }

    // ─── IMPLEMENTACIÓN DE LA INTERFAZ ISmokableItem ────────────────────────

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration % 2 == 0) {
            Vec3 look = entity.getLookAngle();
            double x = entity.getX() + look.x * 0.43;
            double y = entity.getEyeY() - 0.1 + look.y * 0.1;
            double z = entity.getZ() + look.z * 0.43;

            level.addParticle(ParticleTypes.SMOKE, x, y, z, look.x * 0.1, 0.05, look.z * 0.1);

            if (getQuality(stack) >= 2) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, 0, -0.02, 0);
            }
        }
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);

            // 🚀 LLAMADA A NUESTRO SEMÁFORO DE EFECTOS
            this.applyProductEffects(entity, stack);

            if (entity instanceof Player player) {
                if (!player.getAbilities().instabuild) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        stack.shrink(1);
                    }
                }
            }
        }
        return stack;
    }

    // ─── HOVER TEXT (TOOLTIPS DINÁMICOS) ────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String content = getContentType(stack);
        int quality = getQuality(stack);

        Component contentName = switch (content) {
            case "indica" -> Component.literal("Indica").withStyle(ChatFormatting.DARK_GREEN);
            case "sativa" -> Component.literal("Sativa").withStyle(ChatFormatting.GREEN);
            case "mystic_mushroom" -> Component.literal("Mystic Shroom").withStyle(ChatFormatting.DARK_PURPLE);
            default -> Component.literal("Unknown").withStyle(ChatFormatting.GRAY);
        };

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.contains").append(": ").append(contentName));

        ChatFormatting color = switch (quality) {
            case 0 -> ChatFormatting.GRAY;
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.GREEN;
            default -> ChatFormatting.GOLD;
        };

        Component qualityName = switch (quality) {
            case 0 -> Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> Component.translatable("tooltip.alteredstates.quality.regular");
            case 2 -> Component.translatable("tooltip.alteredstates.quality.normal");
            case 3 -> Component.translatable("tooltip.alteredstates.quality.good");
            default -> Component.translatable("tooltip.alteredstates.quality.premium");
        };

        tooltipComponents.add(Component.translatable("tooltip.alteredstates.quality_format", qualityName).withStyle(color));

        int remainingSmokes = stack.getMaxDamage() - stack.getDamageValue();
        tooltipComponents.add(Component.translatable("tooltip.alteredstates.remaining_smokes", remainingSmokes).withStyle(ChatFormatting.DARK_GRAY));
    }
}