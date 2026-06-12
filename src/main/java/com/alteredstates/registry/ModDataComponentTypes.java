package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}