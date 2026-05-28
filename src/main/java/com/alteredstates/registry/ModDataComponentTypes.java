package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
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

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}