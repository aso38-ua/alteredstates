package com.alteredstates.item;

import com.alteredstates.client.render.SmokableRenderer;
import com.alteredstates.component.SmokableData;
import com.alteredstates.component.EmberState;
import com.alteredstates.registry.ModDataComponentTypes;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base de TODO ítem fumable: cigarrillos, puros, porros, pipas...
 *
 * Todos comparten:
 *  - los SmokeVisuals y la plomería de GeckoLib (caché, renderer, controlador de primera persona);
 *  - el consumo por tiempo: componente CIGAR_DATA (combustible), barra de consumo, encendido con mechero,
 *    caladas manteniendo el botón, apagado automático y brasa (EMBER);
 *  - el efecto de la calada: a los PUFF_TICKS (36) ticks de mantener el botón se aplican los efectos del
 *    producto (applyProductEffects), se dispara el logro y suena el humo.
 *
 * Lo que cambia entre ítems se enchufa con métodos que se sobrescriben:
 *    applyProductEffects(...)   efectos del producto (obligatorio)
 *    onPuff(...)                algo extra en cada calada (el cigarrillo se hace daño)
 *    onBurnedOut(...)           qué pasa al agotarse el combustible (por defecto el ítem desaparece;
 *                               una pipa vacía su cazoleta y sigue existiendo)
 *    requiresLighter()          si hace falta mechero para encenderlo (por defecto sí)
 *    isTipVisible(...)          si hay algo que arda (pipa vacía: no)
 *
 * El nombre CigarData viene del cigarro, pero es el estado de combustión de cualquiera.
 */
public abstract class SmokableItem extends Item implements ISmokableItem, GeoItem, Equipable {
    /** Ticks manteniendo el botón para que cuente como calada (efectos, logro y sonido). */
    protected static final long PUFF_TICKS = 36L;
    private static final long HEAD_BURN_RATE = 10L;
    private static final long HEAD_PUFF_INTERVAL = 600L;
    private static final long HEAD_PUFF_DURATION = 10L;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SmokeVisuals visuals;

    protected SmokableItem(Properties properties, SmokeVisuals visuals) {
        super(properties.component(ModDataComponentTypes.CIGAR_DATA.get(), SmokableData.NEW));
        this.visuals = visuals;
        GeoItem.registerSyncedAnimatable(this);
    }

    public SmokeVisuals visuals() {
        return visuals;
    }

    /** Ajuste del modelo equipado en cabeza, en coordenadas locales del renderer. */
    public float headOffsetX() { return 0.0f; }
    public float headOffsetY() { return 0.0f; }
    public float headOffsetZ() { return 0.0f; }

    @Override
    public SmokeAnimation getSmokeAnimation(ItemStack stack) {
        return visuals.thirdPerson();
    }

    // ------------------------------------------------------------------------------------------
    // Hooks por ítem
    // ------------------------------------------------------------------------------------------

    /** Efectos del producto al dar una calada (servidor). */
    public abstract void applyProductEffects(LivingEntity entity, ItemStack stack);

    /** Efectos suaves refrescados mientras el objeto encendido está equipado en cabeza. */
    protected void applyHeadEffects(LivingEntity entity, ItemStack stack) {
    }

    /** Algo extra en cada calada, después de los efectos (servidor). Por defecto nada. */
    protected void onPuff(Level level, LivingEntity entity, ItemStack stack) {
    }

    /** Se agotó el combustible (servidor). Por defecto el ítem desaparece. */
    protected void onBurnedOut(ItemStack stack, Level level, Entity holder) {
        stack.shrink(1);
    }

    /** Si hace falta mechero (pedernal o bola de fuego en la otra mano) para encenderlo. */
    protected boolean requiresLighter() {
        return true;
    }

    // ------------------------------------------------------------------------------------------
    // Estado de combustión (componente CIGAR_DATA)
    // ------------------------------------------------------------------------------------------

    protected SmokableData getData(ItemStack stack) {
        SmokableData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        return data != null ? data : SmokableData.NEW;
    }

    protected void setData(ItemStack stack, SmokableData data) {
        stack.set(ModDataComponentTypes.CIGAR_DATA.get(), data);
    }

    /** Parte que queda: 1 = intacto, 0 = consumido. */
    public static float getBurnProgress(ItemStack stack, @Nullable Level level) {
        SmokableData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        if (data == null) {
            data = SmokableData.NEW;
        }
        long now = level != null ? level.getGameTime() : data.anchorTick();
        return Math.max(0.0f, Math.min(1.0f, data.progress(now)));
    }

    public static boolean isLit(ItemStack stack, @Nullable Level level) {
        SmokableData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        if (data == null) {
            data = SmokableData.NEW;
        }
        return data.isLit(level != null ? level.getGameTime() : data.anchorTick());
    }

    @Override
    public float burnProgress(ItemStack stack, @Nullable Level level) {
        return getBurnProgress(stack, level);
    }

    // ------------------------------------------------------------------------------------------
    // Uso: encender, caladas, apagar
    // ------------------------------------------------------------------------------------------

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /** Uso "infinito": mantener el botón es dar caladas; la barra de consumo es el combustible. */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Mayús + clic: colocarlo en su ranura de equipo (la boca, para cigarrillos y pipas) si es Equipable.
        if (player.isShiftKeyDown() && this instanceof Equipable) {
            if (!level.isClientSide) {
                EquipmentSlot slot = ((Equipable) this).getEquipmentSlot();
                ItemStack previous = player.getItemBySlot(slot);
                player.setItemSlot(slot, stack);
                player.setItemInHand(hand, previous);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }

        long now = level.getGameTime();
        SmokableData data = getData(stack);
        if (!data.isLit(now)) {
            if (isLighterInOtherHand(player, hand)) {
                ignite(player, stack, hand, level);
                return InteractionResultHolder.consume(stack);
            }
            if (requiresLighter()) {
                return InteractionResultHolder.fail(stack);
            }
            if (!level.isClientSide) {
                setData(stack, data.light(now));
                markLit(stack, level);
            }
        }
        return this.startSmoking(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            this.spawnSmokeParticles(level, entity, stack, remainingUseDuration);
            return;
        }
        long now = level.getGameTime();
        SmokableData data = getData(stack);
        if (!data.isLit(now)) {
            entity.stopUsingItem();
            return;
        }
        setSmoking(stack, true, level);
        int held = this.getUseDuration(stack, entity) - remainingUseDuration;
        setData(stack, data.drag(now));
        if ((long) held == PUFF_TICKS) {
            if (entity instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) entity, stack);
            }
            this.applyProductEffects(entity, stack);
            this.onPuff(level, entity, stack);
            this.playSmokingSound(level, entity, stack);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            setSmoking(stack, false, level);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
        if (!level.isClientSide) {
            setSmoking(stack, false, level);
        }
    }

    /** Apaga la brasa si ya no se está fumando, apaga el ítem al caducar el encendido y gestiona el agotado. */
    /*@Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) {
            return;
        }
        EmberState ember = this.emberState(stack);
        boolean activelySmoking = entity instanceof LivingEntity
                && ((LivingEntity) entity).isUsingItem()
                && ((LivingEntity) entity).getUseItem() == stack;
        if (ember.smoking() && !activelySmoking) {
            setSmoking(stack, false, level);
        }
        long now = level.getGameTime();
        SmokableData data = getData(stack);
        boolean wornOnHead = entity instanceof LivingEntity living
                && living.getItemBySlot(EquipmentSlot.HEAD) == stack;
        if (wornOnHead && data.lit()) {
            SmokableData burning = data;//.headBurn(now, HEAD_BURN_RATE);
            setData(stack, burning);
            if (burning.fuel() <= 0L) {
                this.onBurnedOut(stack, level, entity);
                return;
            }
            boolean puffing = now % HEAD_PUFF_INTERVAL < HEAD_PUFF_DURATION;
            setSmoking(stack, puffing, level);
            this.applyHeadEffects((LivingEntity) entity, stack);
            if (puffing && now % HEAD_PUFF_INTERVAL == 0L) {
                this.playSmokingSound(level, (LivingEntity) entity, stack);
            }
            return;
        }
        if (data.lit() && !data.isLit(now)) {
            if (data.fuelAt(now) <= 0L) {
                this.onBurnedOut(stack, level, entity);
                return;
            }
            setData(stack, data.extinguish(now));
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.2f, 2.0f);
        }
    }*/

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) {
            return;
        }
        long now = level.getGameTime();
        LivingEntity living = entity instanceof LivingEntity l ? l : null;
        SmokableData data = this.getData(stack);
        boolean wornOnHead = living != null && living.getItemBySlot(EquipmentSlot.HEAD) == stack;

        if (wornOnHead && (data.headBurning() || data.isLit(now))) {
            if (!data.headBurning()) {
                data = data.startHeadBurn(now);      // única escritura al empezar a arder en cabeza
                this.setData(stack, data);
            }
            if (data.fuelAt(now) <= 0L) {
                this.onBurnedOut(stack, level, entity);
                return;
            }
            boolean puffing = now % HEAD_PUFF_INTERVAL < HEAD_PUFF_DURATION;
            SmokableItem.setSmoking(stack, puffing, level);   // solo escribe en los flancos on/off
            this.applyHeadEffects(living, stack);
            if (now % HEAD_PUFF_INTERVAL == 0L) {
                this.playSmokingSound(level, living, stack);
            }
            return;
        }

        if (data.headBurning()) {                    // ya no está en la cabeza
            data = data.stopHeadBurn(now);
            this.setData(stack, data);
        }

        boolean activelySmoking = living != null && living.isUsingItem() && living.getUseItem() == stack;
        if (this.emberState(stack).smoking() && !activelySmoking) {
            SmokableItem.setSmoking(stack, false, level);
        }

        if (data.lit() && !data.isLit(now)) {
            if (data.fuelAt(now) <= 0L) {
                this.onBurnedOut(stack, level, entity);
                return;
            }
            this.setData(stack, data.extinguish(now));
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.2f, 2.0f);
        }
    }

    /** Sin esto, cada cambio del combustible haría saltar la animación de re-equipar. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    private boolean isLighterInOtherHand(Player player, InteractionHand hand) {
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        return otherStack.getItem() instanceof FlintAndSteelItem || otherStack.getItem() instanceof FireChargeItem;
    }

    protected void ignite(Player player, ItemStack stack, InteractionHand hand, Level level) {
        if (level.isClientSide) {
            return;
        }
        setData(stack, getData(stack).light(level.getGameTime()));
        markLit(stack, level);
        InteractionHand lighterHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack lighterStack = player.getItemInHand(lighterHand);
        if (level instanceof ServerLevel && player instanceof ServerPlayer) {
            lighterStack.hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) player, item -> {});
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** Línea "queda un X %" de la barra de consumo. Las subclases la añaden llamando a super.appendHoverText al final. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!this.isTipVisible(stack)) {
            return; // nada que consumir (pipa vacía)
        }
        SmokableData data = getData(stack);
        Level level = context.level();
        float progress = level != null ? data.progress(level.getGameTime()) : (float) data.fuel() / (float) SmokableData.MAX_FUEL;
        tooltip.add(Component.translatable("tooltip.alteredstates.burn_remaining", Math.round(progress * 100.0f))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    // ------------------------------------------------------------------------------------------
    // GeckoLib: controlador de primera persona y renderer
    // ------------------------------------------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "first_person_smoke", state -> {
            if (state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                return PlayState.STOP;
            }
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || !player.getMainHandItem().is(this)) {
                return state.setAndContinue(visuals.clips().idle());
            }
            FpClips clips = this.visuals.clips();
            boolean smoking = player.isUsingItem() && player.getUseItem().is(this);
            if (smoking) {
                if (!state.isCurrentAnimation(clips.raiseHold())) {
                    state.resetCurrentAnimation();
                }
                return state.setAndContinue(clips.raiseHold());
            }
            if (state.isCurrentAnimation(clips.raiseHold())) {
                state.resetCurrentAnimation();
                return state.setAndContinue(clips.lowerIdle());
            }
            if (state.isCurrentAnimation(clips.lowerIdle())) {
                return PlayState.CONTINUE;
            }
            return state.setAndContinue(clips.idle());
        }));
    }

    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SmokableRenderer();
                }
                return this.renderer;
            }
        });
    }

    // ------------------------------------------------------------------------------------------
    // Estado de brasa (componente EMBER). Llamar SOLO en el servidor; el componente se sincroniza.
    // ------------------------------------------------------------------------------------------

    /** Empieza (true) o deja de (false) dar caladas: la brasa sube o se apaga con el tiempo. */
    public static void setSmoking(ItemStack stack, boolean smoking, Level level) {
        //System.out.println("setSmoking " + smoking + " tick=" + level.getGameTime() + " client=" + level.isClientSide);
        EmberState old = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
        EmberState now = old.with(smoking, level.getGameTime());
        if (now != old) {
            stack.set(ModDataComponentTypes.EMBER.get(), now);
        }
    }

    /** Marca que el ítem se ha encendido (activa el carbonizado de la brasa). */
    public static void markLit(ItemStack stack, Level level) {
        EmberState state = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
        if (state.litTick() < 0L) {
            stack.set(ModDataComponentTypes.EMBER.get(), state.withLit(level.getGameTime()));
        }
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
