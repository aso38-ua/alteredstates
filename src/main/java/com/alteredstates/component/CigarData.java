package com.alteredstates.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CigarData(boolean lit, int puffsTaken, long lastLitTick) {

    public static final Codec<CigarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("lit").forGetter(CigarData::lit),
            Codec.INT.fieldOf("puffs_taken").forGetter(CigarData::puffsTaken),
            Codec.LONG.fieldOf("last_lit_tick").forGetter(CigarData::lastLitTick)
    ).apply(instance, CigarData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CigarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CigarData::lit,
            ByteBufCodecs.VAR_INT, CigarData::puffsTaken,
            ByteBufCodecs.VAR_LONG, CigarData::lastLitTick,
            CigarData::new
    );

    public CigarData withLit(boolean newLit, long tick) {
        return new CigarData(newLit, this.puffsTaken, tick);
    }

    public CigarData withPuff(long tick) {
        return new CigarData(true, this.puffsTaken + 1, tick);
    }
}
