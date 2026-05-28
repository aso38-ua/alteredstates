package com.alteredstates.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Estado de sustancias del jugador.
 * Se adjunta a cada PlayerEntity via Data Attachment.
 *
 * activeSubstance  — ID de la sustancia activa ("cannabis_indica", "mystica", …)
 * phaseIndex       — Índice de fase actual (-1 = sin efecto)
 * phaseTicks       — Ticks restantes en la fase actual
 * tolerance        — Mapa substanceId → nivel de tolerancia (0-5)
 * lastConsumption  — Mapa substanceId → gameTick del último consumo
 */
public class PlayerSubstanceData {

    // ── Codec para persistencia ──────────────────────────────────
    public static final Codec<PlayerSubstanceData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING
                            .optionalFieldOf("active_substance", "")
                            .forGetter(d -> d.activeSubstance),
                    Codec.INT
                            .optionalFieldOf("phase_index", -1)
                            .forGetter(d -> d.phaseIndex),
                    Codec.INT
                            .optionalFieldOf("phase_ticks", 0)
                            .forGetter(d -> d.phaseTicks),
                    Codec.unboundedMap(Codec.STRING, Codec.INT)
                            .optionalFieldOf("tolerance", Map.of())
                            .forGetter(d -> Map.copyOf(d.tolerance)),
                    Codec.unboundedMap(Codec.STRING, Codec.LONG)
                            .optionalFieldOf("last_consumption", Map.of())
                            .forGetter(d -> Map.copyOf(d.lastConsumption))
            ).apply(instance, PlayerSubstanceData::fromCodec)
    );

    // ── Campos ───────────────────────────────────────────────────
    private String activeSubstance;
    private int    phaseIndex;
    private int    phaseTicks;
    private final Map<String, Integer> tolerance;
    private final Map<String, Long>    lastConsumption;

    // ── Constructor por defecto (jugador nuevo) ──────────────────
    public PlayerSubstanceData() {
        this.activeSubstance  = "";
        this.phaseIndex       = -1;
        this.phaseTicks       = 0;
        this.tolerance        = new HashMap<>();
        this.lastConsumption  = new HashMap<>();
    }

    // ── Constructor codec ────────────────────────────────────────
    private static PlayerSubstanceData fromCodec(
            String activeSubstance, int phaseIndex, int phaseTicks,
            Map<String, Integer> tolerance, Map<String, Long> lastConsumption) {
        PlayerSubstanceData d = new PlayerSubstanceData();
        d.activeSubstance = activeSubstance;
        d.phaseIndex      = phaseIndex;
        d.phaseTicks      = phaseTicks;
        d.tolerance.putAll(tolerance);
        d.lastConsumption.putAll(lastConsumption);
        return d;
    }

    // ── Estado ───────────────────────────────────────────────────
    public boolean isUnderEffect()   { return phaseIndex >= 0 && phaseTicks > 0; }
    public String  getActiveSubstance() { return activeSubstance; }
    public int     getPhaseIndex()   { return phaseIndex; }
    public int     getPhaseTicks()   { return phaseTicks; }

    // ── Tolerancia ───────────────────────────────────────────────
    public int getTolerance(String substanceId) {
        return tolerance.getOrDefault(substanceId, 0);
    }

    public void incrementTolerance(String substanceId) {
        tolerance.merge(substanceId, 1, (current, inc) -> Math.min(current + inc, 5));
    }

    /** Decrementa tolerancia — llamar cada X días de juego */
    public void decrementTolerance(String substanceId) {
        tolerance.computeIfPresent(substanceId, (k, v) -> v <= 1 ? null : v - 1);
    }

    public void resetTolerance(String substanceId) {
        tolerance.remove(substanceId);
    }

    // ── Último consumo ───────────────────────────────────────────
    public long getLastConsumption(String substanceId) {
        return lastConsumption.getOrDefault(substanceId, 0L);
    }

    public void setLastConsumption(String substanceId, long gameTick) {
        lastConsumption.put(substanceId, gameTick);
    }

    // ── Inicio de efecto ─────────────────────────────────────────
    public void startEffect(String substanceId, int firstPhaseTicks) {
        this.activeSubstance = substanceId;
        this.phaseIndex      = 0;
        this.phaseTicks      = firstPhaseTicks;
    }

    // ── Tick — llamar desde PlayerTickEvent ──────────────────────
    public void tick(Player player) {
        if (!isUnderEffect()) return;

        phaseTicks--;
        if (phaseTicks <= 0) {
            // El sistema de fases completo se implementa cuando
            // tengamos SubstanceRegistry. Por ahora limpiamos el estado.
            clearEffect();
        }
    }

    public void advanceToPhase(int newPhaseIndex, int ticks) {
        this.phaseIndex  = newPhaseIndex;
        this.phaseTicks  = ticks;
    }

    public void clearEffect() {
        this.activeSubstance = "";
        this.phaseIndex      = -1;
        this.phaseTicks      = 0;
    }
}