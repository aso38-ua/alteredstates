package com.alteredstates.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PipeType implements StringRepresentable{
    WOOD_PIPE,
    GOLD_PIPE,
    WISDOM_PIPE;

    public static final Codec<PipeType> CODEC = StringRepresentable.fromEnum(PipeType::values);

    public static final StreamCodec<ByteBuf, PipeType> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    i -> PipeType.values()[i],
                    PipeType::ordinal
            );

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
