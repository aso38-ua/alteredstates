package com.alteredstates.substance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Datos individuales de adicción/dependencia para una sustancia concreta en el jugador.
 */
public class SubstanceData {

    public long lastUse = -1_000_000L;
    public int tolerance = 0;       // Nivel de tolerancia acumulada (0..10)
    public int dependency = 0;      // Nivel de dependencia / adicción (0..100)
    public int recentUses = 0;      // Usos recientes (control de sobredosis / abuso)
    public long recentStart = 0L;

    public SubstanceData() {}

    public SubstanceData(long lastUse, int tolerance, int dependency, int recentUses, long recentStart) {
        this.lastUse = lastUse;
        this.tolerance = tolerance;
        this.dependency = dependency;
        this.recentUses = recentUses;
        this.recentStart = recentStart;
    }

    public static final Codec<SubstanceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("lastUse", -1_000_000L).forGetter(d -> d.lastUse),
            Codec.INT.optionalFieldOf("tolerance", 0).forGetter(d -> d.tolerance),
            Codec.INT.optionalFieldOf("dependency", 0).forGetter(d -> d.dependency),
            Codec.INT.optionalFieldOf("recentUses", 0).forGetter(d -> d.recentUses),
            Codec.LONG.optionalFieldOf("recentStart", 0L).forGetter(d -> d.recentStart)
    ).apply(i, SubstanceData::new));
}

