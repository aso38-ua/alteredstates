package com.alteredstates.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum TobaccoLeafType implements StringRepresentable{
    CAPA, CAPOTE, TRIPA;

    public static final Codec<TobaccoLeafType> CODEC = StringRepresentable.fromEnum(TobaccoLeafType::values);

    public static final StreamCodec<ByteBuf, TobaccoLeafType> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    i -> TobaccoLeafType.values()[i],
                    TobaccoLeafType::ordinal
            );

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
