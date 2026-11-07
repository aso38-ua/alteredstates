package com.alteredstates.cocaine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Estado de cocaína por jugador (Data Attachment). Se conserva al morir (dependencia y tolerancia). */
public class CocaineData {
    public static final int PHASE_NONE = 0;
    public static final int PHASE_HIGH = 1;
    public static final int PHASE_CRASH = 2;

    public long lastUse = -1_000_000L;  // game time del último consumo
    public long highStart = 0L;
    public long phaseEnd = 0L;          // fin de la fase actual (subidón o bajón)
    public int phase = PHASE_NONE;
    public int quality = 2;             // calidad que manda en el subidón actual (0-4)
    public int tolerance = 0;           // sube con cada dosis, se resetea tras 5 días sin consumir
    public int dependency = 0;          // 0..100, baja muy despacio
    public int recentUses = 0;          // dosis en la ventana de sobredosis (se reinicia pasado un rato sin tomar)
    public long recentStart = 0L;       // (sin uso, se conserva por compatibilidad con partidas guardadas)
    public int binge = 0;               // dosis de esta tanda: cuantas más, más fuerte el bajón
    public long collapseEnd = 0L;       // fin del desplome por sobredosis (inmóvil)
    public long deathAt = 0L;           // game time de la muerte por sobredosis (0 = ninguna)

    public CocaineData() {}

    public CocaineData(long lastUse, long highStart, long phaseEnd, int phase, int quality,
                       int tolerance, int dependency, int recentUses, long recentStart,
                       int binge, long collapseEnd, long deathAt) {
        this.lastUse = lastUse;
        this.highStart = highStart;
        this.phaseEnd = phaseEnd;
        this.phase = phase;
        this.quality = quality;
        this.tolerance = tolerance;
        this.dependency = dependency;
        this.recentUses = recentUses;
        this.recentStart = recentStart;
        this.binge = binge;
        this.collapseEnd = collapseEnd;
        this.deathAt = deathAt;
    }

    public static final Codec<CocaineData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("lastUse", -1_000_000L).forGetter(d -> d.lastUse),
            Codec.LONG.optionalFieldOf("highStart", 0L).forGetter(d -> d.highStart),
            Codec.LONG.optionalFieldOf("phaseEnd", 0L).forGetter(d -> d.phaseEnd),
            Codec.INT.optionalFieldOf("phase", 0).forGetter(d -> d.phase),
            Codec.INT.optionalFieldOf("quality", 2).forGetter(d -> d.quality),
            Codec.INT.optionalFieldOf("tolerance", 0).forGetter(d -> d.tolerance),
            Codec.INT.optionalFieldOf("dependency", 0).forGetter(d -> d.dependency),
            Codec.INT.optionalFieldOf("recentUses", 0).forGetter(d -> d.recentUses),
            Codec.LONG.optionalFieldOf("recentStart", 0L).forGetter(d -> d.recentStart),
            Codec.INT.optionalFieldOf("binge", 0).forGetter(d -> d.binge),
            Codec.LONG.optionalFieldOf("collapseEnd", 0L).forGetter(d -> d.collapseEnd),
            Codec.LONG.optionalFieldOf("deathAt", 0L).forGetter(d -> d.deathAt)
    ).apply(i, CocaineData::new));
}
