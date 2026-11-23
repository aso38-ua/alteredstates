package com.alteredstates.item;

import com.alteredstates.component.SmokableData;
import com.alteredstates.registry.ModDataComponentTypes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CigaretteItem extends SmokableItem implements ITobaccoProduct, Equipable {
    /** Tipo de daño de la calada; el logro "vicious" y ClientEvents lo usan. */
    public static final ResourceKey<DamageType> CIGARETTE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("alteredstates", "cigarette"));

    public CigaretteItem(Properties properties) {
        this(properties, SmokeVisuals.CIGARETTE);
    }

    public CigaretteItem(Properties properties, SmokeVisuals visuals) {
        super(properties.stacksTo(1).component(ModDataComponentTypes.QUALITY.get(), 1), visuals);
    }

    /** Cada calada del cigarrillo hace 1 de daño (lo que antes hacía onUseTick a los 36 ticks). */
    @Override
    protected void onPuff(Level level, LivingEntity entity, ItemStack stack) {
        entity.hurt(level.damageSources().source(CIGARETTE_DAMAGE), 1.0f);
    }

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        return stack;
    }

    @Override
    public int getQuality(ItemStack stack) {
        Integer quality = stack.get(ModDataComponentTypes.QUALITY.get());
        return quality != null ? quality : 1;
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) {
            return;
        }
        int quality = this.getQuality(stack);
        int duration = 300 * quality;
        int amplifier = quality == 4 ? 2 : 1;
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier));
    }

    /** Índice del sprite de inventario según encendido y consumo (propiedad "visual_state"). */
    public int getVisualStateIndex(ItemStack stack, @Nullable Level level) {
        SmokableData d = this.getData(stack);
        long now = level != null ? level.getGameTime() : d.anchorTick();
        boolean lit = d.isLit(now);
        boolean pristine = !lit && d.fuel() == SmokableData.MAX_FUEL && d.lastLitTick() == 0L;
        if (pristine) {
            return 0;
        }
        int stage = Math.min(3, (int) ((1.0f - d.progress(now)) * 4.0f));
        return lit ? 1 + stage : 5 + stage;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int quality = this.getQuality(stack);
        ChatFormatting color = switch (quality) {
            case 0 -> ChatFormatting.GRAY;
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.GREEN;
            default -> ChatFormatting.GOLD;
        };
        MutableComponent qualityName = switch (quality) {
            case 0 -> Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> Component.translatable("tooltip.alteredstates.quality.regular");
            case 2 -> Component.translatable("tooltip.alteredstates.quality.normal");
            case 3 -> Component.translatable("tooltip.alteredstates.quality.good");
            default -> Component.translatable("tooltip.alteredstates.quality.premium");
        };
        tooltip.add(Component.translatable("tooltip.alteredstates.quality_format", qualityName).withStyle(color));
        super.appendHoverText(stack, context, tooltip, flag); // línea de consumo (común a todos los fumables)
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public float headOffsetY() { return 0.12f; }

    @Override
    protected void applyHeadEffects(LivingEntity entity, ItemStack stack) {
        if (!entity.level().isClientSide) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, true));
        }
    }
}
