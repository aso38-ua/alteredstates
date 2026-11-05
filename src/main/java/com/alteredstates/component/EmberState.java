package com.alteredstates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EmberState(boolean smoking, long changeTick, float levelAtChange, long litTick) {
    public static final EmberState OFF = new EmberState(false, 0L, 0f, -1L);
    private static final float RISE_TICKS = 8f;   // se enciende en ~0,4 s
    private static final float FALL_TICKS = 60f;  // se apaga en ~3 s
    private static final float CHAR_TICKS = 20f;  // se ennegrece en ~1 s

    public static final Codec<EmberState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("smoking").forGetter(EmberState::smoking),
            Codec.LONG.fieldOf("change_tick").forGetter(EmberState::changeTick),
            Codec.FLOAT.fieldOf("level").forGetter(EmberState::levelAtChange),
            Codec.LONG.optionalFieldOf("lit_tick", -1L).forGetter(EmberState::litTick)
    ).apply(i, EmberState::new));

    public static final StreamCodec<ByteBuf, EmberState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EmberState::smoking,
            ByteBufCodecs.VAR_LONG, EmberState::changeTick,
            ByteBufCodecs.FLOAT, EmberState::levelAtChange,
            ByteBufCodecs.VAR_LONG, EmberState::litTick,
            EmberState::new);

    /** Intensidad 0..1 en el instante `now` (ticks de juego con decimales). */
    public float level(float now) {
        float dt = Math.max(0f, now - changeTick);
        return smoking ? Math.min(1f, levelAtChange + dt / RISE_TICKS)
                : Math.max(0f, levelAtChange - dt / FALL_TICKS);
    }

    /** 0 = punta fresca, 1 = totalmente ennegrecida. */
    public float charLevel(float now) {
        return litTick < 0 ? 0f : Math.min(1f, Math.max(0f, (now - litTick) / CHAR_TICKS));
    }

    /** Estado nuevo si cambia `smoking`; si no, este mismo objeto. */
    public EmberState with(boolean nowSmoking, long gameTime) {
        if (nowSmoking == smoking) return this;
        long firstLitTick = litTick < 0 && nowSmoking ? gameTime : litTick;
        return new EmberState(nowSmoking, gameTime, level(gameTime), firstLitTick);
    }

    public EmberState withLit(long gameTime) {
        return new EmberState(smoking, changeTick, levelAtChange, gameTime);
    }
}
