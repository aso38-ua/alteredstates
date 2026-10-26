package com.alteredstates.cocaine;

import com.alteredstates.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Lógica de la cocaína (todo mecánica de juego, sin valores reales):
 *  SUBIDÓN  : a los 10 s -> Velocidad II + Prisa II, el hambre no baja, inmune a Oscuridad. ~4 min (según calidad/tolerancia)
 *  BAJÓN    : 90 s -> Lentitud III, Debilidad II, Hambre III, Náuseas
 *  TOLERANCIA: cada dosis reduce duración y potencia; se resetea tras 5 días de juego sin consumir
 *  DEPENDENCIA / MONO: con dependencia alta, tras medio día sin dosis llegan los síntomas hasta volver a consumir
 *  SOBREDOSIS: 4 dosis en 3 minutos -> veneno (no mata), náuseas, oscuridad y bajón largo
 */
public final class CocaineEffects {
    private CocaineEffects() {}

    private static final int ONSET_TICKS = 200;           // 10 s
    private static final int BASE_HIGH_TICKS = 4800;      // 4 min
    private static final int MAX_HIGH_TICKS = 7200;       // 6 min
    private static final int CRASH_TICKS = 1800;          // 90 s
    private static final int TOLERANCE_RESET = 120_000;   // 5 días de juego
    private static final int OD_WINDOW = 3600;            // 3 min
    private static final int OD_USES = 4;
    private static final int WITHDRAWAL_AFTER = 12_000;   // medio día sin dosis
    private static final int WITHDRAWAL_MIN_DEP = 25;
    private static final float[] QUALITY_MULT = {0.7F, 0.85F, 1.0F, 1.2F, 1.4F};

    private static CocaineData data(ServerPlayer p) {
        return p.getData(ModAttachments.COCAINE_DATA);
    }

    /** Se llama al terminar de esnifar. */
    public static void use(ServerPlayer p, int quality) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();

        if (now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;
        if (now - d.recentStart > OD_WINDOW) { d.recentUses = 0; d.recentStart = now; }
        d.recentUses++;

        float qMult = QUALITY_MULT[Math.max(0, Math.min(4, quality))];
        float tolMult = Math.max(0.35F, 1.0F - 0.15F * d.tolerance);
        int duration = Math.round(BASE_HIGH_TICKS * qMult * tolMult);

        if (d.phase == CocaineData.PHASE_HIGH) {
            d.phaseEnd = Math.min(d.phaseEnd + duration, now + MAX_HIGH_TICKS);
        } else {
            // si estaba en bajón, la dosis lo corta: ciclo vicioso
            d.phase = CocaineData.PHASE_HIGH;
            d.highStart = now;
            d.phaseEnd = now + duration;
        }

        d.tolerance = Math.min(d.tolerance + 1, 10);
        d.dependency = Math.min(100, d.dependency + 10 + 2 * d.tolerance);
        d.lastUse = now;

        p.displayClientMessage(Component.literal("Un escalofrío te recorre la nariz...").withStyle(ChatFormatting.GRAY), true);

        if (d.recentUses >= OD_USES) overdose(p, d, now);
    }

    private static void overdose(ServerPlayer p, CocaineData d, long now) {
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
        d.phase = CocaineData.PHASE_CRASH;
        d.phaseEnd = now + CRASH_TICKS * 2L;
        d.recentUses = 0;
        p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        p.displayClientMessage(Component.literal("¡El corazón te va a mil! Demasiado, demasiado rápido...").withStyle(ChatFormatting.DARK_RED), true);
    }

    private static void apply(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amp) {
        p.addEffect(new MobEffectInstance(effect, 60, amp, true, false, true));
    }

    /** Cada tick de jugador (solo servidor). */
    public static void tick(ServerPlayer p) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();

        if (d.tolerance > 0 && now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;

        // ── SUBIDÓN ──
        if (d.phase == CocaineData.PHASE_HIGH) {
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_CRASH;
                d.phaseEnd = now + CRASH_TICKS;
                p.displayClientMessage(Component.literal("Se te va el subidón... bajón.").withStyle(ChatFormatting.DARK_GRAY), true);
                p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, true, false, true));
            } else if (now - d.highStart >= ONSET_TICKS) {
                int amp = d.tolerance >= 3 ? 0 : 1; // con tolerancia, el efecto baja de II a I
                p.getFoodData().setExhaustion(0.0F);
                if (now % 20 == 0) {
                    apply(p, MobEffects.MOVEMENT_SPEED, amp);
                    apply(p, MobEffects.DIG_SPEED, amp);
                    p.removeEffect(MobEffects.DARKNESS);
                }
                if (now % 6 == 0 && p.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WAX_ON, p.getX(), p.getY() + 1.0D, p.getZ(), 1, 0.3D, 0.5D, 0.3D, 0.0D);
                }
                if (now % 120 == 0 && d.tolerance >= 2) {
                    p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.4F, 1.5F);
                }
            }
            return;
        }

        // ── BAJÓN ──
        if (d.phase == CocaineData.PHASE_CRASH) {
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_NONE;
            } else {
                if (now % 20 == 0) {
                    apply(p, MobEffects.MOVEMENT_SLOWDOWN, 2);
                    apply(p, MobEffects.WEAKNESS, 1);
                    apply(p, MobEffects.HUNGER, 2);
                }
                if (now % 600 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, true));
            }
            return;
        }

        // ── Sin consumir: dependencia y mono ──
        if (d.dependency > 0 && now - d.lastUse > 24_000 && now % 4800 == 0) {
            d.dependency--;
        }
        if (d.dependency >= WITHDRAWAL_MIN_DEP && now - d.lastUse > WITHDRAWAL_AFTER) {
            int severity = 1 + d.dependency / 40; // 1..3
            if (now % 40 == 0) {
                apply(p, MobEffects.MOVEMENT_SLOWDOWN, Math.max(0, severity - 2));
                apply(p, MobEffects.DIG_SLOWDOWN, severity - 1);
                if (severity >= 2) apply(p, MobEffects.WEAKNESS, severity - 2);
            }
            if (severity >= 3 && now % 900 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, true));
            if (now % 2400 == 0) {
                p.displayClientMessage(Component.literal("Te tiemblan las manos... necesitas otra dosis.").withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
