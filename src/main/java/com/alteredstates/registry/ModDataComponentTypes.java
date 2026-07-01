package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.component.CigarData;
import com.alteredstates.item.CigarType;
import com.alteredstates.item.PipeType;
import com.alteredstates.item.TobaccoLeafType;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AlteredStates.MOD_ID);

    // Registramos la calidad como un número entero persistente (se guarda en el disco del mundo)
    public static final Supplier<DataComponentType<Integer>> QUALITY =
            DATA_COMPONENT_TYPES.register("quality", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT) // Indica a Minecraft cómo guardar este dato en el archivo de la partida
                    .build());

    // 🌿 Guarda un Verdadero/Falso para saber si el consumible base era Indica (true) o Sativa (false)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_INDICA =
            DATA_COMPONENT_TYPES.register("is_indica", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TobaccoLeafType>> TOBACCO_LEAF_TYPE =
            DATA_COMPONENT_TYPES.register("tobacco_leaf_type", () ->
                    DataComponentType.<TobaccoLeafType>builder()
                            .persistent(TobaccoLeafType.CODEC)
                            .networkSynchronized(TobaccoLeafType.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CONTENT_TYPE =
            DATA_COMPONENT_TYPES.register("content_type", () ->
                    DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8) // Necesario para los tooltips
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CigarType>> CIGAR_TYPE =
            DATA_COMPONENT_TYPES.register("cigar_type", () ->
                    DataComponentType.<CigarType>builder()
                            .persistent(CigarType.CODEC)
                            .networkSynchronized(CigarType.STREAM_CODEC)
                            .build()
            );

    public static final Supplier<DataComponentType<CigarData>> CIGAR_DATA =
            DATA_COMPONENT_TYPES.register("cigar_data", () -> DataComponentType.<CigarData>builder()
                    .persistent(CigarData.CODEC)
                    .networkSynchronized(CigarData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PipeType>> PIPE_TYPE =
            DATA_COMPONENT_TYPES.register("pipe_type", () ->
                    DataComponentType.<PipeType>builder()
                            .persistent(PipeType.CODEC)
                            .networkSynchronized(PipeType.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> PIPE_CONTENT =
            DATA_COMPONENT_TYPES.register("pipe_content", () ->
                    DataComponentType.<ItemStack>builder()
                            .persistent(ItemStack.CODEC)
                            .networkSynchronized(ItemStack.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}