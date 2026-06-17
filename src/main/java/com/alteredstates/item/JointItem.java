package com.alteredstates.item;

import com.alteredstates.item.CannabisStrain;
import com.alteredstates.item.ICannabisProduct;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.util.SmokingEffectProcessor;
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

public class JointItem extends Item implements ISmokableItem, ICannabisProduct {

    private final boolean isIndica;

    public JointItem(Properties properties, boolean isIndica) {
        // Asignamos por defecto calidad 1 (Normal) y la cepa base en los componentes del ItemStack
        super(properties.stacksTo(1).durability(4)
                .component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.IS_INDICA.get(), isIndica));
        this.isIndica = isIndica;
    }

    // ─── IMPLEMENTACIÓN DE INTERFACES MODULARES ─────────────────────────────

    @Override
    public int getQuality(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
    }

    @Override
    public CannabisStrain getStrain(ItemStack stack) {
        boolean indica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), this.isIndica);
        return CannabisStrain.fromBoolean(indica);
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) return;

        // Obtenemos los datos de forma limpia a través de la interfaz
        int quality = getQuality(stack);
        com.alteredstates.item.CannabisStrain strain = getStrain(stack);

        // ⏱️ Efectos buenos: 45 segundos base por nivel de calidad (Ej: Calidad 3 = 2 minutos y pico)
        int goodDuration = 800 * quality;
        int amplifier = quality >= 3 ? 1 : 0;

        // Comprobamos la cepa usando nuestro nuevo Enum unificado
        if (strain == com.alteredstates.item.CannabisStrain.INDICA) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.INDICA_EFFECT, goodDuration, amplifier));
            // Bonus Premium: Regeneración cortita
            if (quality >= 3) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 300, 1));
            }
        } else if (strain == com.alteredstates.item.CannabisStrain.SATIVA) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.SATIVA_EFFECT, goodDuration, amplifier));
            // Bonus Premium: Prisa minera cortita
            if (quality >= 3) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 400, 1));
            }
        }

        // 💀 Paranoia: Probabilidad base (10%), sube si la calidad es baja. Duración muy corta (15-30 segs)
        float paranoiaChance = quality <= 1 ? 0.20f : 0.05f;
        if (entity.level().random.nextFloat() < paranoiaChance) {
            int badDuration = 600 + (100 * quality); // 30 a 45 segundos
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.alteredstates.registry.ModEffects.PARANOIA, badDuration, 0));
        }
    }

    // ─── LÓGICA DE JUEGO (FUMAR) ────────────────────────────────────────────

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
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        this.spawnSmokeParticles(level, entity, stack, remainingUseDuration);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration % 2 == 0) {
            Vec3 look = entity.getLookAngle();
            double x = entity.getX() + look.x * 0.43;
            double y = entity.getEyeY() - 0.1 + look.y * 0.1;
            double z = entity.getZ() + look.z * 0.43;

            level.addParticle(ParticleTypes.SMOKE, x, y, z, look.x * 0.1, 0.05, look.z * 0.1);

            // Usamos el método getQuality() de nuestra interfaz
            if (getQuality(stack) >= 2) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, 0, -0.02, 0);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return this.onSmokeFinished(stack, level, entity);
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);

            // 🚀 LLAMADA MODULAR: El objeto ejecuta sus propios efectos automáticamente
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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        // Usamos el método getQuality() unificado
        int quality = getQuality(stack);

        net.minecraft.ChatFormatting color = switch (quality) {
            case 0 -> net.minecraft.ChatFormatting.GRAY;
            case 1 -> net.minecraft.ChatFormatting.WHITE;
            case 2 -> net.minecraft.ChatFormatting.AQUA;
            case 3 -> net.minecraft.ChatFormatting.GREEN;
            default -> net.minecraft.ChatFormatting.GOLD;
        };

        net.minecraft.network.chat.Component qualityName = switch (quality) {
            case 0 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.regular");
            case 2 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.normal");
            case 3 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.good");
            default -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.premium");
        };

        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality_format", qualityName)
                .withStyle(color));

        int remainingSmokes = stack.getMaxDamage() - stack.getDamageValue();
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.remaining_smokes", remainingSmokes)
                .withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
    }
}