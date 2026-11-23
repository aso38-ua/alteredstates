package com.alteredstates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SmokableData(boolean lit, long fuel, long anchorTick, long lastLitTick) {

    public static final long MAX_FUEL = 60_000L;
    /** Fuel por tick mientras se mantiene la calada (incluye el 1 del pasivo). 150 => 20 s el cigarro entero. */
    public static final long HELD_RATE = 150L;
    public static final long AUTO_EXTINGUISH_TICKS = 6000L;
    public static final SmokableData NEW = new SmokableData(false, MAX_FUEL, 0L, 0L);

    public static final Codec<SmokableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("lit").forGetter(SmokableData::lit),
            Codec.LONG.fieldOf("fuel").forGetter(SmokableData::fuel),
            Codec.LONG.fieldOf("anchor_tick").forGetter(SmokableData::anchorTick),
            Codec.LONG.fieldOf("last_lit_tick").forGetter(SmokableData::lastLitTick)
    ).apply(instance, SmokableData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmokableData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SmokableData::lit,
            ByteBufCodecs.VAR_LONG, SmokableData::fuel,
            ByteBufCodecs.VAR_LONG, SmokableData::anchorTick,
            ByteBufCodecs.VAR_LONG, SmokableData::lastLitTick,
            SmokableData::new
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

    /** 0 = recién fumado, 1 = ceniza completa (apagado). 0 si nunca se ha encendido. */
    public float ashLevel(float now) {
        if (this.lastLitTick <= 0L) return 0.0f;
        if (!this.lit || this.fuelAt((long) now) <= 0L) return 1.0f;
        float t = (now - (float) this.lastLitTick) / (float) AUTO_EXTINGUISH_TICKS;
        return Math.min(1.0f, Math.max(0.0f, t));
    }

    public SmokableData light(long now) {
        return new SmokableData(true, fuelAt(now), now, now);
    }

    /** Un tick de calada: re-ancla y descuenta el extra del boost. Solo lo llama onUseTick en el servidor. */
    public SmokableData drag(long now) {
        return new SmokableData(true, Math.max(0, fuelAt(now) - (HELD_RATE - 1)), now, now);
    }

    public SmokableData headBurn(long now, long rate) {
        return new SmokableData(true, Math.max(0, fuelAt(now) - Math.max(0, rate - 1)), now, now);
    }

    public SmokableData extinguish(long now) {
        return new SmokableData(false, fuelAt(now), now, lastLitTick);
    }
}
