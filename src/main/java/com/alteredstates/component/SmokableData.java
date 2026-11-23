package com.alteredstates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SmokableData(boolean lit, long fuel, long anchorTick, long lastLitTick, boolean headBurning) {
    public static final long HEAD_RATE = 10L;

    public SmokableData(boolean lit, long fuel, long anchorTick, long lastLitTick) {
        this(lit, fuel, anchorTick, lastLitTick, false);
    }
    // ... resto de constantes (MAX_FUEL, HELD_RATE, AUTO_EXTINGUISH_TICKS, NEW)
    public static final long MAX_FUEL = 60_000L;
    /** Fuel por tick mientras se mantiene la calada (incluye el 1 del pasivo). 150 => 20 s el cigarro entero. */
    public static final long HELD_RATE = 150L;
    public static final long AUTO_EXTINGUISH_TICKS = 6000L;
    public static final SmokableData NEW = new SmokableData(false, MAX_FUEL, 0L, 0L);

    public static final Codec<SmokableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("lit").forGetter(SmokableData::lit),
            Codec.LONG.fieldOf("fuel").forGetter(SmokableData::fuel),
            Codec.LONG.fieldOf("anchor_tick").forGetter(SmokableData::anchorTick),
            Codec.LONG.fieldOf("last_lit_tick").forGetter(SmokableData::lastLitTick),
            Codec.BOOL.optionalFieldOf("head_burning", false).forGetter(SmokableData::headBurning)
    ).apply(instance, SmokableData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmokableData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SmokableData::lit,
            ByteBufCodecs.VAR_LONG, SmokableData::fuel,
            ByteBufCodecs.VAR_LONG, SmokableData::anchorTick,
            ByteBufCodecs.VAR_LONG, SmokableData::lastLitTick,
            ByteBufCodecs.BOOL, SmokableData::headBurning,
            SmokableData::new);


    public float progress(long now) { return (float) fuelAt(now) / MAX_FUEL; } // 1 = nuevo, 0 = consumido

    public long fuelAt(long now) {
        if (!this.lit) {
            return this.fuel;
        }
        if (this.headBurning) {
            return Math.max(0L, this.fuel - Math.max(0L, now - this.anchorTick) * HEAD_RATE);
        }
        long burnEnd = Math.min(now, this.lastLitTick + AUTO_EXTINGUISH_TICKS);
        return Math.max(0L, this.fuel - Math.max(0L, burnEnd - this.anchorTick));
    }

    public boolean isLit(long now) {
        return this.lit
                && (this.headBurning || now < this.lastLitTick + AUTO_EXTINGUISH_TICKS)
                && this.fuelAt(now) > 0L;
    }

    public float ashLevel(float now) {
        if (this.lastLitTick <= 0L) {
            return 0.0f;
        }
        if (this.lit && this.headBurning && this.fuelAt((long) now) > 0L) {
            return 0.0f; // en cabeza no se acumula ceniza por tiempo (antes lastLitTick se reseteaba cada tick)
        }
        if (!this.lit || this.fuelAt((long) now) <= 0L) {
            return 1.0f;
        }
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


    public SmokableData startHeadBurn(long now) {
        return new SmokableData(true, this.fuelAt(now), now, now, true);
    }

    public SmokableData stopHeadBurn(long now) {
        return new SmokableData(true, this.fuelAt(now), now, now, false);
    }

    public SmokableData extinguish(long now) {
        return new SmokableData(false, fuelAt(now), now, lastLitTick);
    }
}
