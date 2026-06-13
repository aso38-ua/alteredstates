package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.block.entity.DryingRackBlockEntity;
import com.alteredstates.block.entity.RollingTrayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AlteredStates.MOD_ID);

    public static final Supplier<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK =
            BLOCK_ENTITIES.register("drying_rack", () ->
                    BlockEntityType.Builder.of(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK.get()).build(null));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<com.alteredstates.block.entity.CuringJarBlockEntity>> CURING_JAR =
            BLOCK_ENTITIES.register("curing_jar", () ->
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                            com.alteredstates.block.entity.CuringJarBlockEntity::new,
                            com.alteredstates.registry.ModBlocks.CURING_JAR.get() // ¡Asegúrate de que apunte a tu bloque!
                    ).build(null));

    public static final Supplier<BlockEntityType<RollingTrayBlockEntity>> ROLLING_TRAY =
            BLOCK_ENTITIES.register("rolling_tray", () ->
                    BlockEntityType.Builder.of(RollingTrayBlockEntity::new, ModBlocks.ROLLING_TRAY.get()).build(null));

    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<com.alteredstates.block.entity.BongBlockEntity>> BONG = BLOCK_ENTITIES.register("bong",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(com.alteredstates.block.entity.BongBlockEntity::new, com.alteredstates.registry.ModBlocks.BONG.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}