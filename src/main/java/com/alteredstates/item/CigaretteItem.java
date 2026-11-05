package com.alteredstates.item;

import com.alteredstates.component.CigarData;
import com.alteredstates.component.EmberState;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class CigaretteItem extends Item implements ISmokableItem, ITobaccoProduct, GeoItem, Equipable {

    /** Ticks mantenidos para que la calada dé efecto (una vez por calada). */
    private static final long EFFECT_TICKS = 36L;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final ResourceKey<DamageType> CIGARETTE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("alteredstates", "cigarette"));

    public CigaretteItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.CIGAR_DATA.get(), CigarData.NEW));
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) renderer = new com.alteredstates.client.render.CigaretteRenderer();
                return renderer;
            }
        });
    }


    // ------------------------------------------------------------------ estado

    private CigarData getData(ItemStack stack) {
        CigarData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        return data != null ? data : CigarData.NEW;
    }

    public static float getBurnProgress(ItemStack stack, Level level) {
        CigarData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        if (data == null) data = CigarData.NEW;
        long now = level != null ? level.getGameTime() : data.anchorTick();
        return Math.max(0f, Math.min(1f, data.progress(now)));
    }

    public static boolean isLit(ItemStack stack, Level level) {
        CigarData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        if (data == null) data = CigarData.NEW;
        return data.isLit(level != null ? level.getGameTime() : data.anchorTick());
    }

    private void setData(ItemStack stack, CigarData data) {
        stack.set(ModDataComponentTypes.CIGAR_DATA.get(), data);
    }

    @Override
    public SmokeAnimation getSmokeAnimation(ItemStack stack) {
        return SmokeAnimation.CIGARETTE;
    }
    // --------------------------------------------------------------- uso / humo

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /** Enorme a propósito: la calada la cortas tú soltando, no vanilla al llegar a 0. */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift + clic derecho equipa el cigarrillo en la cabeza; si ya había un objeto,
        // intercambia ambos objetos sin perder sus componentes.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack previousHeadItem = player.getItemBySlot(EquipmentSlot.HEAD);
                player.setItemSlot(EquipmentSlot.HEAD, stack);
                player.setItemInHand(hand, previousHeadItem);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }

        CigarData d = getData(stack);
        long now = level.getGameTime();

        if (!d.isLit(now)) {
            if (isLighterInOtherHand(player, hand)) {
                igniteCigarette(player, stack, hand, level);
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.fail(stack);
        }
        return startSmoking(level, player, hand);   // ya no escribe nada al empezar
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide) return;
        setSmoking(stack, true, level);
        long now = level.getGameTime();
        CigarData d = getData(stack);

        if (!d.isLit(now)) {            // se apagó o se agotó
            entity.stopUsingItem();
            return;
        }

        int held = getUseDuration(stack, entity) - remaining;   // ticks mantenidos en esta calada
        setData(stack, d.drag(now));                             // el boost se paga aquí, tick a tick

        if (held == EFFECT_TICKS) {                              // una sola vez por calada
            if (entity instanceof ServerPlayer sp) CriteriaTriggers.CONSUME_ITEM.trigger(sp, stack);
            applyProductEffects(entity, stack);
            entity.hurt(level.damageSources().source(CIGARETTE_DAMAGE), 1.0F);
            playSmokingSound(level, entity, stack);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;
        EmberState ember = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
        boolean activelySmoking = entity instanceof LivingEntity living && living.isUsingItem()
                && living.getUseItem() == stack;
        if (ember.smoking() && !activelySmoking) setSmoking(stack, false, level);
        long now = level.getGameTime();
        CigarData d = getData(stack);

        if (d.lit() && !d.isLit(now)) {
            if (d.fuelAt(now) <= 0) {   // se acabó (aquí podrías dejar una colilla)
                stack.shrink(1);
                return;
            }
            setData(stack, d.extinguish(now));
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.2F, 2.0F);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide) setSmoking(stack, false, level);
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) setSmoking(stack, false, level);
        return stack;
    }

    /** Evita que cada cambio del componente haga rebotar el item en primera persona. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    // ------------------------------------------------------------------ encendido

    private boolean isLighterInOtherHand(Player player, InteractionHand hand) {
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        return otherStack.getItem() instanceof FlintAndSteelItem
                || otherStack.getItem() instanceof FireChargeItem;
    }

    private void igniteCigarette(Player player, ItemStack cigaretteStack, InteractionHand cigaretteHand, Level level) {
        if (level.isClientSide) return; // todo el efecto es del servidor

        setData(cigaretteStack, getData(cigaretteStack).light(level.getGameTime()));
        markLit(cigaretteStack, level);

        InteractionHand lighterHand = cigaretteHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack lighterStack = player.getItemInHand(lighterHand);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            lighterStack.hurtAndBreak(1, serverLevel, serverPlayer, item -> {});
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // ------------------------------------------------- ISmokableItem / ITobaccoProduct

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // Pendiente: fase de humo (hilo constante + bocanada)
    }

    /** Legado de la interfaz: con useDuration de 72000 la calada ya no "termina" por sí sola. */
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
        if (entity.level().isClientSide) return;

        int quality = getQuality(stack);
        int duration = 300 * quality;
        int amplifier = quality == 4 ? 2 : 1;

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier));
    }

    // ------------------------------------------------------------ visual / tooltip

    /**
     * Transitorio: mapea el progreso continuo a los índices 0-8 que usa tu ModItemProperties,
     * hasta que el modelo pase a GeckoLib y el desgaste se dibuje directamente con progress().
     */
    public int getVisualStateIndex(ItemStack stack, @Nullable Level level) {
        CigarData d = getData(stack);
        long now = level != null ? level.getGameTime() : d.anchorTick();
        boolean lit = d.isLit(now);

        boolean pristine = !lit && d.fuel() == CigarData.MAX_FUEL && d.lastLitTick() == 0L;
        if (pristine) return 0;

        int stage = Math.min(3, (int) ((1.0F - d.progress(now)) * 4));
        return lit ? 1 + stage : 5 + stage;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int quality = getQuality(stack);

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

        tooltip.add(Component.translatable("tooltip.alteredstates.quality_format", qualityName).withStyle(color));

        CigarData d = getData(stack);
        Level level = context.level();
        float progress = level != null ? d.progress(level.getGameTime()) : (float) d.fuel() / CigarData.MAX_FUEL;
        tooltip.add(Component.translatable("tooltip.alteredstates.burn_remaining", Math.round(progress * 100))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    public static void setSmoking(ItemStack stack, boolean smoking, Level level) {
        EmberState old = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
        EmberState now = old.with(smoking, level.getGameTime());
        if (now != old) stack.set(ModDataComponentTypes.EMBER.get(), now);
    }

    public static void markLit(ItemStack stack, Level level) {
        EmberState state = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
        if (state.litTick() < 0) {
            stack.set(ModDataComponentTypes.EMBER.get(), state.withLit(level.getGameTime()));
        }
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
