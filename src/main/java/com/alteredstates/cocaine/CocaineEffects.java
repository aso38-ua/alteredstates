package com.alteredstates.cocaine;

import com.alteredstates.registry.ModAttachments;
import com.alteredstates.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Cocaína (mecánicas de juego inventadas):
 *  CALIDAD manda: Velocidad = calidad + 1 (Q0 = I ... Q4 = V), Prisa = calidad (Q1 = I ... Q4 = IV),
 *                 Salto desde Q2. Duración 6 min (Q0) hasta 15 min (Q4).
 *  SUBIDÓN : a los 10 s empieza; el hambre no baja, inmune a Oscuridad, overlay suave tipo éxtasis.
 *  BAJÓN   : cuanto mejor la dosis, más largo y duro (Lentitud, Debilidad, Hambre, overlay apagado).
 *  Los efectos duran lo que marca el HUD y se pueden quitar (leche, /effect clear): al limpiar el
 *  marcador de la fase, el subidón/bajón se corta. El mono NO se quita con leche: es dependencia.
 *  TOLERANCIA: cada 2 dosis seguidas pierdes un nivel de potencia y duran menos; se resetea a los 5 días de juego.
 *  MONO    : con dependencia alta, tras medio día sin dosis llegan los síntomas hasta volver a consumir.
 *  SOBREDOSIS: 4 dosis en 3 minutos -> veneno (no mata), náuseas, oscuridad y bajón doble.
 */
public final class CocaineEffects {
    private CocaineEffects() {}

    /** Pon true cuando tengas los iconos textures/mob_effect/cocaine_rush.png y cocaine_crash.png */
    private static final boolean SHOW_ICON = false;

    private static final int ONSET_TICKS = 200;           // 10 s
    private static final int BASE_HIGH_TICKS = 12000;     // 10 min con calidad Normal
    private static final int MAX_HIGH_TICKS = 24000;      // tope 20 min acumulando dosis
    private static final int CRASH_BASE_TICKS = 1800;     // 90 s + 15 s por nivel de calidad
    private static final int CRASH_PER_QUALITY = 300;
    private static final int TOLERANCE_RESET = 120_000;   // 5 días de juego
    private static final int OD_WINDOW = 3600;            // 3 min
    private static final int OD_USES = 4;
    private static final int WITHDRAWAL_AFTER = 12_000;   // medio día sin dosis
    private static final int WITHDRAWAL_MIN_DEP = 25;
    private static final float[] DURATION_BY_QUALITY = {0.6F, 0.8F, 1.0F, 1.25F, 1.5F};

    private static CocaineData data(ServerPlayer p) {
        return p.getData(ModAttachments.COCAINE_DATA);
    }

    private static int clampQ(int q) { return Math.max(0, Math.min(4, q)); }

    /** Potencia efectiva: la calidad menos 1 nivel por cada 2 de tolerancia. */
    private static int power(CocaineData d) {
        return Math.max(0, clampQ(d.quality) - d.tolerance / 2);
    }

    /** Se llama al terminar de esnifar. */
    public static void use(ServerPlayer p, int quality) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();
        int q = clampQ(quality);

        if (now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;
        if (now - d.recentStart > OD_WINDOW) { d.recentUses = 0; d.recentStart = now; }
        d.recentUses++;

        float tolMult = Math.max(0.4F, 1.0F - 0.12F * d.tolerance);
        int duration = Math.round(BASE_HIGH_TICKS * DURATION_BY_QUALITY[q] * tolMult);

        if (d.phase == CocaineData.PHASE_HIGH) {
            d.phaseEnd = Math.min(d.phaseEnd + duration, now + MAX_HIGH_TICKS);
            d.quality = Math.max(d.quality, q);
        } else {
            // si estaba en bajón, la dosis lo corta: ciclo vicioso
            d.phase = CocaineData.PHASE_HIGH;
            d.highStart = now;
            d.phaseEnd = now + duration;
            d.quality = q;
        }

        d.tolerance = Math.min(d.tolerance + 1, 10);
        d.dependency = Math.min(100, d.dependency + 8 + 3 * q + 2 * d.tolerance);
        d.lastUse = now;

        ensure(p, ModEffects.COCAINE_RUSH, 0, d.phaseEnd - now, SHOW_ICON);
        p.displayClientMessage(Component.literal("Un escalofrío te recorre la nariz...").withStyle(ChatFormatting.GRAY), true);

        if (d.recentUses >= OD_USES) overdose(p, d, now);
    }

    private static void overdose(ServerPlayer p, CocaineData d, long now) {
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
        d.phase = CocaineData.PHASE_CRASH;
        d.highStart = now;
        d.phaseEnd = now + (CRASH_BASE_TICKS + CRASH_PER_QUALITY * clampQ(d.quality)) * 2L;
        d.recentUses = 0;
        p.removeEffect(ModEffects.COCAINE_RUSH);
        ensure(p, ModEffects.COCAINE_CRASH, 0, d.phaseEnd - now, SHOW_ICON);
        p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        p.displayClientMessage(Component.literal("¡El corazón te va a mil! Demasiado, demasiado rápido...").withStyle(ChatFormatting.DARK_RED), true);
    }

    /** Efecto corto que se refresca (mono): se ve en el HUD con 00:02 mientras dure la abstinencia. */
    private static void apply(ServerPlayer p, Holder<MobEffect> effect, int amp) {
        p.addEffect(new MobEffectInstance(effect, 60, amp, true, false, false));
    }

    private static final int END_MARGIN = 40;

    /** Aplica el efecto con la duración REAL restante (solo si falta, es más débil o dura menos). */
    private static void ensure(ServerPlayer p, Holder<MobEffect> effect, int amp, long remaining, boolean icon) {
        int duration = (int) Math.max(20L, remaining) + END_MARGIN;
        MobEffectInstance cur = p.getEffect(effect);
        if (cur == null || cur.getAmplifier() < amp || cur.getDuration() < duration - 100) {
            p.addEffect(new MobEffectInstance(effect, duration, amp, true, false, icon));
        }
    }

    /** Cada tick de jugador (solo servidor). */
    public static void tick(ServerPlayer p) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();

        if (d.tolerance > 0 && now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;

        // ── SUBIDÓN ──
        if (d.phase == CocaineData.PHASE_HIGH) {
            // Alguien limpió el marcador (leche, /effect clear): se acaba el subidón
            if (now - d.highStart > 40 && !p.hasEffect(ModEffects.COCAINE_RUSH)) {
                d.phase = CocaineData.PHASE_NONE;
                p.displayClientMessage(Component.literal("Se te corta el subidón de golpe.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_CRASH;
                d.highStart = now;
                d.phaseEnd = now + CRASH_BASE_TICKS + (long) CRASH_PER_QUALITY * clampQ(d.quality);
                p.removeEffect(ModEffects.COCAINE_RUSH);
                ensure(p, ModEffects.COCAINE_CRASH, 0, d.phaseEnd - now, SHOW_ICON);
                p.displayClientMessage(Component.literal("Se te va el subidón... bajón.").withStyle(ChatFormatting.DARK_GRAY), true);
                p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, true, false, false));
            } else if (now - d.highStart >= ONSET_TICKS) {
                int q = clampQ(d.quality);
                int pw = power(d);
                p.getFoodData().setExhaustion(0.0F);
                if (now % 20 == 0) {
                    long left = d.phaseEnd - now;
                    ensure(p, MobEffects.MOVEMENT_SPEED, pw, left, true);                 // Q1 -> Velocidad II, Q2 -> III...
                    ensure(p, MobEffects.DIG_SPEED, Math.max(0, pw - 1), left, true);     // Prisa un nivel por debajo
                    if (q >= 2) ensure(p, MobEffects.JUMP, pw >= 4 ? 1 : 0, left, true);
                    ensure(p, ModEffects.COCAINE_RUSH, 0, left, SHOW_ICON);
                    p.removeEffect(MobEffects.DARKNESS);
                }
                if (now % 6 == 0 && p.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WAX_ON, p.getX(), p.getY() + 1.0D, p.getZ(), 1 + pw / 2, 0.3D, 0.5D, 0.3D, 0.0D);
                }
                if (now % 120 == 0 && d.tolerance >= 2) {
                    p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.4F, 1.5F);
                }
            }
            return;
        }

        // ── BAJÓN ──
        if (d.phase == CocaineData.PHASE_CRASH) {
            if (now - d.highStart > 40 && !p.hasEffect(ModEffects.COCAINE_CRASH)) {
                d.phase = CocaineData.PHASE_NONE;   // limpiado con leche o /effect clear
                return;
            }
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_NONE;
            } else {
                int q = clampQ(d.quality);
                if (now % 20 == 0) {
                    long left = d.phaseEnd - now;
                    ensure(p, MobEffects.MOVEMENT_SLOWDOWN, 1 + (q + 1) / 2, left, true); // Q0:II Q1-2:III Q3-4:IV
                    ensure(p, MobEffects.WEAKNESS, q >= 3 ? 2 : 1, left, true);
                    ensure(p, MobEffects.HUNGER, 2, left, true);
                    ensure(p, ModEffects.COCAINE_CRASH, 0, left, SHOW_ICON);
                }
                if (now % 600 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, false));
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
            if (severity >= 3 && now % 900 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, false));
            if (now % 2400 == 0) {
                p.displayClientMessage(Component.literal("Te tiemblan las manos... necesitas otra dosis.").withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
