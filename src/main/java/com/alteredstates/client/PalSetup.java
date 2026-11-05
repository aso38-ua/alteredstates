package com.alteredstates.client;

import com.alteredstates.AlteredStates;
import com.alteredstates.item.ISmokableItem;
import com.alteredstates.item.SmokeAnimation;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public final class PalSetup {

    public static final ResourceLocation SMOKE_LAYER =
            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "smoking_layer");

    /** Duración en ticks de los clips <id>_raise y <id>_lower. Deben coincidir con el JSON. */
    //private static final int RAISE_TICKS = 7;
    //private static final int LOWER_TICKS = 7;

    private enum Phase { IDLE, SMOKING, LOWERING }

    /** Estado por jugador: la factory se invoca una vez por jugador. */
    private static final class SmokeState {
        Phase phase = Phase.IDLE;
        SmokeAnimation anim;
        int startTick;
        int lowerEndTick;
    }

    private record Clips(RawAnimation start, RawAnimation lower) {}

    // Siempre las mismas instancias, para que PAL no reinicie la animación cada frame
    private static final Map<SmokeAnimation, Clips> CLIPS = new EnumMap<>(SmokeAnimation.class);

    private PalSetup() {}

    private static ResourceLocation suffixed(ResourceLocation base, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + suffix);
    }

    private static Clips clipsFor(SmokeAnimation anim) {
        return CLIPS.computeIfAbsent(anim, a -> {
            ResourceLocation id = a.id();
            RawAnimation start = PlayerRawAnimationBuilder.begin()
                    .then(suffixed(id, "_raise"), Animation.LoopType.PLAY_ONCE)
                    .then(suffixed(id, "_hold"), Animation.LoopType.LOOP)
                    .build();
            RawAnimation lower = PlayerRawAnimationBuilder.begin()
                    .then(suffixed(id, "_lower"), Animation.LoopType.PLAY_ONCE)
                    .build();
            return new Clips(start, lower);
        });
    }

    /** La animación de fumar que corresponde ahora a esta entidad, o null si no está fumando. */
    private static SmokeAnimation currentSmokeAnimation(LivingEntity entity) {
        if (!entity.isUsingItem()) return null;
        ItemStack stack = entity.getUseItem();
        return stack.getItem() instanceof ISmokableItem smokable ? smokable.getSmokeAnimation(stack) : null;
    }

    public static void register() {
        AlteredStates.LOGGER.info("[PAL debug] register()");
        registerSmokeLayer();
    }

    private static void registerSmokeLayer() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(SMOKE_LAYER, 1500, player -> {
            final SmokeState st = new SmokeState();

            PlayerAnimationController controller = new PlayerAnimationController(player, (c, state, animSetter) -> {
                SmokeAnimation current = currentSmokeAnimation(player);
                boolean smoking = current != null;

                switch (st.phase) {
                    case IDLE -> {
                        if (!smoking) {
                            c.forceAnimationReset();
                            return PlayState.STOP;
                        }
                        st.phase = Phase.SMOKING;
                        st.anim = current;
                        st.startTick = player.tickCount;
                        return animSetter.setAnimation(clipsFor(current).start());   // raise -> hold en bucle
                    }
                    case SMOKING -> {
                        if (smoking) return PlayState.CONTINUE;
                        SmokeAnimation a = st.anim;
                        // Si soltó a mitad del raise, el lower arranca en el punto equivalente para que no haya salto
                        float f = Math.min(1f, (player.tickCount - st.startTick) / (float) a.raiseTicks());
                        int from = Math.round(a.lowerTicks() * (1f - f));
                        st.phase = Phase.LOWERING;
                        st.lowerEndTick = player.tickCount + (a.lowerTicks() - from) + 1;
                        return animSetter.setAnimation(clipsFor(a).lower(), from);
                    }
                    case LOWERING -> {
                        if (smoking) {                    // volvió a pulsar mientras bajaba
                            st.phase = Phase.SMOKING;
                            st.anim = current;
                            st.startTick = player.tickCount;
                            return animSetter.setAnimation(clipsFor(current).start());
                        }
                        if (player.tickCount >= st.lowerEndTick) {
                            st.phase = Phase.IDLE;
                            c.forceAnimationReset();
                            return PlayState.STOP;         // devuelve el brazo a vanilla
                        }
                        return PlayState.CONTINUE;
                    }
                }
                return PlayState.STOP;
            });

            controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            controller.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true));
            controller.setFirstPersonFollowsCamera(true);
            //controller.addModifierLast(new FirstPersonBodyOffset(1.92f));
            return controller;
        });
    }
}