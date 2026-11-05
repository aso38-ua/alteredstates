package com.alteredstates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CigarData(boolean lit, long fuel, long anchorTick, long lastLitTick) {

    public static final long MAX_FUEL = 60_000L;
    /** Fuel por tick mientras se mantiene la calada (incluye el 1 del pasivo). 150 => 20 s el cigarro entero. */
    public static final long HELD_RATE = 150L;
    public static final long AUTO_EXTINGUISH_TICKS = 6000L;
    public static final CigarData NEW = new CigarData(false, MAX_FUEL, 0L, 0L);

    public static final Codec<CigarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("lit").forGetter(CigarData::lit),
            Codec.LONG.fieldOf("fuel").forGetter(CigarData::fuel),
            Codec.LONG.fieldOf("anchor_tick").forGetter(CigarData::anchorTick),
            Codec.LONG.fieldOf("last_lit_tick").forGetter(CigarData::lastLitTick)
    ).apply(instance, CigarData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CigarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CigarData::lit,
            ByteBufCodecs.VAR_LONG, CigarData::fuel,
            ByteBufCodecs.VAR_LONG, CigarData::anchorTick,
            ByteBufCodecs.VAR_LONG, CigarData::lastLitTick,
            CigarData::new
    );

    /** Combustible en 'now' (solo quema pasiva, acotada por el timer de apagado). No muta nada. */
    public long fuelAt(long now) {
        if (!lit) return fuel;
        long burnEnd = Math.min(now, lastLitTick + AUTO_EXTINGUISH_TICKS);
        return Math.max(0, fuel - Math.max(0, burnEnd - anchorTick));
    }

    public boolean isLit(long now) {
        return lit && now < lastLitTick + AUTO_EXTINGUISH_TICKS && fuelAt(now) > 0;
    }

    public float progress(long now) { return (float) fuelAt(now) / MAX_FUEL; } // 1 = nuevo, 0 = consumido

    public CigarData light(long now) {
        return new CigarData(true, fuelAt(now), now, now);
    }

    /** Un tick de calada: re-ancla y descuenta el extra del boost. Solo lo llama onUseTick en el servidor. */
    public CigarData drag(long now) {
        return new CigarData(true, Math.max(0, fuelAt(now) - (HELD_RATE - 1)), now, now);
    }

    public CigarData extinguish(long now) {
        return new CigarData(false, fuelAt(now), now, lastLitTick);
    }
}