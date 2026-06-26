package com.alteredstates.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum CigarType implements StringRepresentable{
    DON_JAVIER,
    ESPLENDIDO,
    TORITO,
    LANCERO;

    public static final Codec<CigarType> CODEC = StringRepresentable.fromEnum(CigarType::values);

    public static final StreamCodec<ByteBuf, CigarType> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    i -> CigarType.values()[i],
                    CigarType::ordinal
            );

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
