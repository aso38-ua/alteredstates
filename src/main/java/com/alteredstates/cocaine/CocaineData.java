package com.alteredstates.cocaine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Estado de cocaína por jugador (Data Attachment). Se conserva al morir. */
public class CocaineData {
    public static final int PHASE_NONE = 0;
    public static final int PHASE_HIGH = 1;
    public static final int PHASE_CRASH = 2;

    public long lastUse = -1_000_000L;  // game time del último consumo
    public long highStart = 0L;
    public long phaseEnd = 0L;          // fin de la fase actual (subidón o bajón)
    public int phase = PHASE_NONE;
    public int tolerance = 0;           // sube con cada dosis, se resetea tras 5 días sin consumir
    public int dependency = 0;          // 0..100, baja muy despacio
    public int recentUses = 0;          // dosis dentro de la ventana de sobredosis
    public long recentStart = 0L;

    public CocaineData() {}

    public CocaineData(long lastUse, long highStart, long phaseEnd, int phase,
                       int tolerance, int dependency, int recentUses, long recentStart) {
        this.lastUse = lastUse;
        this.highStart = highStart;
        this.phaseEnd = phaseEnd;
        this.phase = phase;
        this.tolerance = tolerance;
        this.dependency = dependency;
        this.recentUses = recentUses;
        this.recentStart = recentStart;
    }

    public static final Codec<CocaineData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("lastUse", -1_000_000L).forGetter(d -> d.lastUse),
            Codec.LONG.optionalFieldOf("highStart", 0L).forGetter(d -> d.highStart),
            Codec.LONG.optionalFieldOf("phaseEnd", 0L).forGetter(d -> d.phaseEnd),
            Codec.INT.optionalFieldOf("phase", 0).forGetter(d -> d.phase),
            Codec.INT.optionalFieldOf("tolerance", 0).forGetter(d -> d.tolerance),
            Codec.INT.optionalFieldOf("dependency", 0).forGetter(d -> d.dependency),
            Codec.INT.optionalFieldOf("recentUses", 0).forGetter(d -> d.recentUses),
            Codec.LONG.optionalFieldOf("recentStart", 0L).forGetter(d -> d.recentStart)
    ).apply(i, CocaineData::new));
}
