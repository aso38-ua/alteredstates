package com.alteredstates.cocaine;

import com.alteredstates.registry.ModAttachments;
import com.alteredstates.registry.ModEffects;
import com.alteredstates.substance.SubstanceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Cocaína (mecánicas de juego inventadas):
 *  CALIDAD manda: Velocidad = calidad + 1 (Q0 = I ... Q4 = V), Prisa = calidad - 1, Salto desde Q2.
 *  SUBIDÓN : a los 10 s empieza y se ACUMULA con cada dosis (tope 20 min). Primero lo bueno...
 *  BAJÓN   : ...y luego llega. Dura y pega más cuantas más dosis hayas tomado en la tanda ("binge") y cuanto mejor la calidad.
 *  RULETA RUSA: desde la 2ª dosis dentro de la ventana, cada dosis tiene un 20 % de sobredosis (OD_CHANCE).
 *    Sobredosis -> se acaba lo bueno, te desplomas (quieto unos 8 s) y se tira la ruleta:
 *      20 % muerte súbita entre 0,1 y 5 s después | 30 % Wither (muerte lenta, la leche o curarte te salva)
 *      50 % Veneno fuerte (casi mueres, pero aguantas) | y siempre un bajón doble.
 *  VENTANA: las dosis se reinician DOSE_WINDOW ticks después de la última.
 *  TOLERANCIA: cada 2 dosis seguidas pierdes un nivel de potencia; se resetea a los 5 días de juego.
 *  MONO    : dependencia alta + mucho tiempo sin dosis -> síntomas (ver SubstanceManager).
 */
public final class CocaineEffects {
    private CocaineEffects() {}

    /** Pon true cuando tengas los iconos textures/mob_effect/cocaine_rush.png y cocaine_crash.png */
    private static final boolean SHOW_ICON = false;

    // ───────── Ajustes de balance (todo en ticks salvo indicación) ─────────
    private static final int ONSET_TICKS = 200;           // 10 s
    private static final int BASE_HIGH_TICKS = 12000;     // 10 min con calidad Normal
    private static final int MAX_HIGH_TICKS = 24000;      // tope 20 min acumulando dosis
    private static final int CRASH_BASE_TICKS = 1800;     // 90 s + 15 s por nivel de calidad
    private static final int CRASH_PER_QUALITY = 300;
    private static final float BINGE_CRASH_STEP = 0.35F;  // +35 % de bajón por cada dosis extra de la tanda
    private static final int MAX_CRASH_TICKS = 9000;      // tope 7,5 min
    private static final int TOLERANCE_RESET = 120_000;   // 5 días de juego
    private static final int DOSE_WINDOW = 12_000;        // las dosis se reinician 10 min después de la última
    private static final int OD_MIN_DOSES = 2;            // la ruleta empieza en la 2ª dosis de la ventana
    private static final float OD_CHANCE = 0.20F;         // 20 % por dosis
    private static final int OD_DEATH = 20;               // % de muerte súbita (pesos sobre 100)
    private static final int OD_WITHER = 30;              // % de Wither; el resto = veneno (sobrevives)
    private static final int COLLAPSE_TICKS = 160;        // 8 s inmóvil
    private static final int DEATH_DELAY_MIN = 2;         // 0,1 s
    private static final int DEATH_DELAY_MAX = 100;       // 5 s
    private static final int WITHDRAWAL_AFTER = 12_000;   // medio día sin dosis
    private static final int WITHDRAWAL_MIN_DEP = 25;
    private static final float[] DURATION_BY_QUALITY = {0.6F, 0.8F, 1.0F, 1.25F, 1.5F};

    private static final ResourceLocation COLLAPSE_ID = ResourceLocation.fromNamespaceAndPath("alteredstates", "collapse");
    public static final ResourceKey<DamageType> OVERDOSE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("alteredstates", "overdose"));

    private static CocaineData data(ServerPlayer p) {
        return p.getData(ModAttachments.COCAINE_DATA);
    }

    private static int clampQ(int q) { return Math.max(0, Math.min(4, q)); }

    /** Potencia efectiva: la calidad menos 1 nivel por cada 2 de tolerancia. */
    private static int power(CocaineData d) {
        return Math.max(0, clampQ(d.quality) - d.tolerance / 2);
    }

    private static long crashTicks(CocaineData d) {
        long base = CRASH_BASE_TICKS + (long) CRASH_PER_QUALITY * clampQ(d.quality);
        float mult = 1.0F + BINGE_CRASH_STEP * Math.max(0, d.binge - 1);
        return Math.min(MAX_CRASH_TICKS, Math.round(base * mult));
    }

    // ───────────────────────── Consumo ─────────────────────────

    /** Se llama al terminar de esnifar. */
    public static void use(ServerPlayer p, int quality) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();
        int q = clampQ(quality);

        if (now < d.collapseEnd) return; // desplomado: no puede consumir

        if (now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;
        if (now - d.lastUse > DOSE_WINDOW) d.recentUses = 0;
        d.recentUses++;

        boolean fromCrash = d.phase == CocaineData.PHASE_CRASH;
        if (d.phase == CocaineData.PHASE_NONE) d.binge = 0;
        d.binge++;

        float tolMult = Math.max(0.4F, 1.0F - 0.12F * d.tolerance);
        int duration = Math.round(BASE_HIGH_TICKS * DURATION_BY_QUALITY[q] * tolMult);

        if (d.phase == CocaineData.PHASE_HIGH) {
            d.phaseEnd = Math.min(d.phaseEnd + duration, now + MAX_HIGH_TICKS);
            d.quality = Math.max(d.quality, q);
        } else {
            if (fromCrash) clearCrashEffects(p); // la dosis corta el bajón: ciclo vicioso
            d.phase = CocaineData.PHASE_HIGH;
            d.highStart = now;
            d.phaseEnd = now + duration;
            d.quality = q;
        }

        d.tolerance = Math.min(d.tolerance + 1, 10);
        d.dependency = Math.min(100, d.dependency + 8 + 3 * q + 2 * d.tolerance);
        d.lastUse = now;
        SubstanceManager.clearWithdrawalSuppression(p);

        ensure(p, ModEffects.COCAINE_RUSH, 0, d.phaseEnd - now, SHOW_ICON);
        p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.rush").withStyle(ChatFormatting.GRAY), true);

        // 🎲 Ruleta rusa
        if (d.recentUses >= OD_MIN_DOSES) {
            if (p.getRandom().nextFloat() < OD_CHANCE) {
                overdose(p, d, now);
            } else {
                p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.8F, 1.2F);
                p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.heart").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private static void overdose(ServerPlayer p, CocaineData d, long now) {
        // Se acabó lo bueno
        p.removeEffect(ModEffects.COCAINE_RUSH);
        p.removeEffect(MobEffects.MOVEMENT_SPEED);
        p.removeEffect(MobEffects.DIG_SPEED);
        p.removeEffect(MobEffects.JUMP);

        // Desplome: quieto, sin ver bien, sin poder saltar ni picar
        d.collapseEnd = now + COLLAPSE_TICKS;
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, COLLAPSE_TICKS, 7, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, COLLAPSE_TICKS, 4, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, COLLAPSE_TICKS + 100, 0, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, COLLAPSE_TICKS, 0, true, false, false));
        applyCollapseModifier(p);

        // Bajón doble, el doble de duro cuanto más hayas tomado
        d.phase = CocaineData.PHASE_CRASH;
        d.highStart = now;
        d.phaseEnd = now + crashTicks(d) * 2L;
        ensure(p, ModEffects.COCAINE_CRASH, 0, d.phaseEnd - now, SHOW_ICON);

        p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.6F);
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.5F);
        p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.overdose").withStyle(ChatFormatting.DARK_RED), true);

        // 🎲 ¿Qué te toca?
        int roll = p.getRandom().nextInt(100);
        if (roll < OD_DEATH) {
            // Muerte súbita entre 0,1 y 5 s después
            d.deathAt = now + DEATH_DELAY_MIN + p.getRandom().nextInt(DEATH_DELAY_MAX - DEATH_DELAY_MIN + 1);
        } else if (roll < OD_DEATH + OD_WITHER) {
            // Muerte lenta y dolorosa (se puede evitar curándote o con leche, que quita el Wither)
            p.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1));
            p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.overdose.wither").withStyle(ChatFormatting.DARK_GRAY), true);
        } else {
            // Casi mueres, pero aguantas: el veneno nunca mata, te deja casi sin vida
            p.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 2));
            p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.overdose.survive").withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static void killByOverdose(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        Holder<DamageType> type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OVERDOSE);
        p.hurt(new DamageSource(type), Float.MAX_VALUE);
    }

    // ───────────────────────── Desplome ─────────────────────────

    private static void applyCollapseModifier(ServerPlayer p) {
        AttributeInstance jump = p.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null && !jump.hasModifier(COLLAPSE_ID)) {
            jump.addTransientModifier(new AttributeModifier(COLLAPSE_ID, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeCollapseModifier(ServerPlayer p) {
        AttributeInstance jump = p.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) jump.removeModifier(COLLAPSE_ID);
    }

    public static boolean isCollapsed(ServerPlayer p) {
        return p.level().getGameTime() < data(p).collapseEnd;
    }

    /** Si el jugador muere (de lo que sea): se limpia lo pendiente. Dependencia y tolerancia se conservan. */
    public static void onDeath(ServerPlayer p) {
        CocaineData d = data(p);
        d.deathAt = 0L;
        d.collapseEnd = 0L;
        d.phase = CocaineData.PHASE_NONE;
        d.recentUses = 0;
        d.binge = 0;
        removeCollapseModifier(p);
    }

    // ───────────────────────── Efectos con duración real ─────────────────────────

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

    /** Efectos del bajón; más fuertes con más dosis en la tanda. */
    private static void applyCrashEffects(ServerPlayer p, CocaineData d, long left) {
        int q = clampQ(d.quality);
        int extra = d.binge >= 4 ? 1 : 0;
        ensure(p, MobEffects.MOVEMENT_SLOWDOWN, Math.min(4, 1 + (q + 1) / 2 + extra), left, true);
        ensure(p, MobEffects.WEAKNESS, Math.min(3, (q >= 3 ? 2 : 1) + extra), left, true);
        ensure(p, MobEffects.HUNGER, d.binge >= 3 ? 3 : 2, left, true);
        ensure(p, ModEffects.COCAINE_CRASH, 0, left, SHOW_ICON);
    }

    private static void clearCrashEffects(ServerPlayer p) {
        p.removeEffect(ModEffects.COCAINE_CRASH);
        p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        p.removeEffect(MobEffects.WEAKNESS);
        p.removeEffect(MobEffects.HUNGER);
    }

    // ───────────────────────── Tick ─────────────────────────

    /** Cada tick de jugador (solo servidor). */
    public static void tick(ServerPlayer p) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();

        // ☠ Muerte súbita pendiente
        if (d.deathAt > 0 && now >= d.deathAt) {
            d.deathAt = 0L;
            if (p.isAlive() && !p.isCreative() && !p.isSpectator()) killByOverdose(p);
            return;
        }

        // Desplome: mientras dure, no se puede saltar
        if (d.collapseEnd > 0) {
            if (now < d.collapseEnd) applyCollapseModifier(p);
            else { removeCollapseModifier(p); d.collapseEnd = 0L; }
        }

        if (d.tolerance > 0 && now - d.lastUse > TOLERANCE_RESET) d.tolerance = 0;

        // ── SUBIDÓN ──
        if (d.phase == CocaineData.PHASE_HIGH) {
            // Alguien limpió el marcador (leche, /effect clear): se acaba el subidón
            if (now - d.highStart > 40 && !p.hasEffect(ModEffects.COCAINE_RUSH)) {
                d.phase = CocaineData.PHASE_NONE;
                d.binge = 0;
                p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.cut").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_CRASH;
                d.highStart = now;
                d.phaseEnd = now + crashTicks(d);
                p.removeEffect(ModEffects.COCAINE_RUSH);
                applyCrashEffects(p, d, d.phaseEnd - now);
                p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.crash").withStyle(ChatFormatting.DARK_GRAY), true);
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
                if (now % 120 == 0 && (d.tolerance >= 2 || d.binge >= 2)) {
                    p.level().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.4F, 1.5F);
                }
            }
            return;
        }

        // ── BAJÓN ──
        if (d.phase == CocaineData.PHASE_CRASH) {
            if (now - d.highStart > 40 && !p.hasEffect(ModEffects.COCAINE_CRASH)) {
                if (SubstanceManager.didDrinkMilkRecently(p)) {
                    // Bebió leche: la leche NO quita el bajón, se re-aplica
                    applyCrashEffects(p, d, Math.max(20L, d.phaseEnd - now));
                    p.displayClientMessage(Component.translatable("message.alteredstates.milk_no_effect").withStyle(ChatFormatting.DARK_RED), true);
                    return;
                }
                d.phase = CocaineData.PHASE_NONE;   // limpiado con /effect clear
                d.binge = 0;
                return;
            }
            if (now >= d.phaseEnd) {
                d.phase = CocaineData.PHASE_NONE;
                d.binge = 0;
            } else {
                if (now % 20 == 0) applyCrashEffects(p, d, d.phaseEnd - now);
                if (now % 600 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, false));
            }
            return;
        }

        // ── Sin consumir: dependencia y mono ──
        if (d.dependency > 0 && now - d.lastUse > 24_000 && now % 4800 == 0) {
            d.dependency--;
        }
        if (d.dependency >= WITHDRAWAL_MIN_DEP && now - d.lastUse > WITHDRAWAL_AFTER
                && !SubstanceManager.isWithdrawalSuppressed(p)) {
            int severity = 1 + d.dependency / 40; // 1..3
            if (now % 40 == 0) {
                apply(p, MobEffects.MOVEMENT_SLOWDOWN, Math.max(0, severity - 2));
                apply(p, MobEffects.DIG_SLOWDOWN, severity - 1);
                if (severity >= 2) apply(p, MobEffects.WEAKNESS, severity - 2);
            }
            if (severity >= 3 && now % 900 == 0) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false, false));
            if (now % 2400 == 0) {
                p.displayClientMessage(Component.translatable("message.alteredstates.cocaine.withdrawal").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    // ───────────────────────── Consultas y reset ─────────────────────────

    public static int getTolerance(ServerPlayer p) { return data(p).tolerance; }
    public static int getDependency(ServerPlayer p) { return data(p).dependency; }

    /** Dosis dentro de la ventana actual (0 si ya pasó el tiempo de reinicio). */
    public static int getDoses(ServerPlayer p) {
        CocaineData d = data(p);
        return p.level().getGameTime() - d.lastUse > DOSE_WINDOW ? 0 : d.recentUses;
    }

    /** Riesgo de sobredosis (en %) de la PRÓXIMA dosis. */
    public static int getOverdoseRiskPercent(ServerPlayer p) {
        return getDoses(p) + 1 >= OD_MIN_DOSES ? Math.round(OD_CHANCE * 100) : 0;
    }

    /** Segundos que faltan para que el contador de dosis vuelva a 0. */
    public static int getDoseResetSeconds(ServerPlayer p) {
        if (getDoses(p) == 0) return 0;
        long left = DOSE_WINDOW - (p.level().getGameTime() - data(p).lastUse);
        return (int) Math.max(0L, left / 20L);
    }

    /** Mono activo: fuera de subidón/bajón, dependencia alta y mucho tiempo sin dosis. */
    public static boolean hasWithdrawal(ServerPlayer p, long now) {
        CocaineData d = data(p);
        return d.phase == CocaineData.PHASE_NONE
                && d.dependency >= WITHDRAWAL_MIN_DEP && now - d.lastUse > WITHDRAWAL_AFTER;
    }

    /** Pone todo a cero y quita los marcadores de fase. */
    public static void reset(ServerPlayer p) {
        removeCollapseModifier(p);
        p.setData(ModAttachments.COCAINE_DATA, new CocaineData());
        p.removeEffect(ModEffects.COCAINE_RUSH);
        p.removeEffect(ModEffects.COCAINE_CRASH);
    }

    public static void onMilkDrank(ServerPlayer p) {
        CocaineData d = data(p);
        long now = p.level().getGameTime();
        if (d.phase == CocaineData.PHASE_CRASH) {
            applyCrashEffects(p, d, Math.max(20L, d.phaseEnd - now));
            p.displayClientMessage(Component.translatable("message.alteredstates.milk_no_effect").withStyle(ChatFormatting.DARK_RED), true);
        } else if (d.dependency >= WITHDRAWAL_MIN_DEP && now - d.lastUse > WITHDRAWAL_AFTER
                && !SubstanceManager.isWithdrawalSuppressed(p)) {
            int severity = 1 + d.dependency / 40;
            apply(p, MobEffects.MOVEMENT_SLOWDOWN, Math.max(0, severity - 2));
            apply(p, MobEffects.DIG_SLOWDOWN, severity - 1);
            if (severity >= 2) apply(p, MobEffects.WEAKNESS, severity - 2);
            p.displayClientMessage(Component.translatable("message.alteredstates.milk_no_effect").withStyle(ChatFormatting.DARK_RED), true);
        }
    }
}
