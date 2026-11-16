package com.alteredstates.item;

import com.alteredstates.client.render.SmokableRenderer;
import com.alteredstates.component.EmberState;
import com.alteredstates.registry.ModDataComponentTypes;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base de todo ítem fumable con modelo geo: cigarrillos, puros, pipas...
 *
 * Aquí vive lo que antes estaba pegado a CigaretteItem y no tiene nada de cigarrillo:
 *  - los SmokeVisuals (qué modelo/textura/animaciones usa),
 *  - la plomería de GeckoLib (caché, renderer),
 *  - el controlador de primera persona (idle / raise+hold al fumar / lower al soltar),
 *  - los helpers del estado de brasa (setSmoking / markLit).
 *
 * Un ítem nuevo solo hereda de esta clase y pasa sus SmokeVisuals. El resto (consumo, encendido,
 * efectos) es de cada ítem.
 */
public abstract class SmokableItem extends Item implements ISmokableItem, GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SmokeVisuals visuals;

    protected SmokableItem(Properties properties, SmokeVisuals visuals) {
        super(properties);
        this.visuals = visuals;
        GeoItem.registerSyncedAnimatable(this);
    }

    public SmokeVisuals visuals() {
        return visuals;
    }

    @Override
    public SmokeAnimation getSmokeAnimation(ItemStack stack) {
        return visuals.thirdPerson();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "first_person_smoke", state -> {
            if (state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                return PlayState.STOP;
            }
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || !player.getMainHandItem().is(this)) {
                return PlayState.STOP;
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
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) setSmoking(stack, true, level);    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) setSmoking(stack, false, level);
        return stack;
    }


    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
        if (!level.isClientSide) setSmoking(stack, false, level);
    }


}
