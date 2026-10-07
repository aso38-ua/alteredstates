package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    // Registros
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, AlteredStates.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, AlteredStates.MOD_ID);

    // Asumo que tienes un ModBlocks, pero si no, creamos un registro de bloques para el fluido
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AlteredStates.MOD_ID);

    // 1. Tipo de fluido: Aquí controlamos las físicas. Viscosidad alta (como lava) pero temperatura normal (no quema)
    /*public static final DeferredHolder<FluidType, FluidType> HORSE_SEMEN_FLUID_TYPE = FLUID_TYPES.register("horse_semen",
            () -> new FluidType(FluidType.Properties.create()
                    .density(3000)      // Más denso que el agua (1000)
                    .viscosity(6000)    // Altamente viscoso (hace que te muevas lento dentro de él)
                    .temperature(300)   // Temperatura normal, evita que queme
                    .canPushEntity(true))); // Empuja a las entidades

    // 2. Bloque de origen y bloque fluyendo
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> HORSE_SEMEN_SOURCE = FLUIDS.register("horse_semen_source",
            () -> new BaseFlowingFluid.Source(getFluidProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> HORSE_SEMEN_FLOWING = FLUIDS.register("horse_semen_flowing",
            () -> new BaseFlowingFluid.Flowing(getFluidProperties()));

    // 3. El bloque físico que se coloca en el mundo
    public static final DeferredHolder<Block, LiquidBlock> HORSE_SEMEN_BLOCK = BLOCKS.register("horse_semen_block",
            () -> new LiquidBlock(HORSE_SEMEN_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                            .noLootTable()
                            .liquid()));

    // 4. Conectar todas las partes y configurar cómo se expande
    private static BaseFlowingFluid.Properties getFluidProperties() {
        return new BaseFlowingFluid.Properties(
                HORSE_SEMEN_FLUID_TYPE,
                HORSE_SEMEN_SOURCE,
                HORSE_SEMEN_FLOWING)
                .block(HORSE_SEMEN_BLOCK)
                .bucket(ModItems.HORSE_SEMEN)
                .slopeFindDistance(2)       // Como la lava: no viaja muy lejos (agua = 4)
                .levelDecreasePerBlock(2);  // Como la lava: baja 2 niveles por bloque que avanza
    }*/
}