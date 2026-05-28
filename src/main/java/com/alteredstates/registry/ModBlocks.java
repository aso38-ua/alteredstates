package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.block.CuringJarBlock;
import com.alteredstates.block.DryingRackBlock;
import com.alteredstates.block.IndicaCropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AlteredStates.MOD_ID);

    // ════════════════════════════════════════════════════════════
    //  CANNABIS - Cultivos
    // ════════════════════════════════════════════════════════════
    public static final DeferredBlock<Block> INDICA_CROP = BLOCKS.register("indica_crop",
            () -> new IndicaCropBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)));

    // ════════════════════════════════════════════════════════════
    //  CANNABIS
    // ════════════════════════════════════════════════════════════
    // Se añadirán en el módulo cannabis:
    //   INDICA_CROP, SATIVA_CROP_LOWER, SATIVA_CROP_UPPER, HYBRID_CROP
    //   DRYING_RACK
    public static final DeferredBlock<Block> DRYING_RACK = BLOCKS.register("drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of()
                    .noOcclusion() // Importante para bloques que no son cubos completos
                    .strength(2.0f)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> CURING_JAR = BLOCKS.register("curing_jar",
            () -> new CuringJarBlock(BlockBehaviour.Properties.of()
                    .noOcclusion() // Imprescindible para modelos transparentes o más pequeños de 16x16
                    .strength(1.0f) // Un poco más frágil que la madera
                    .sound(SoundType.GLASS))); // ¡Que suene a cristal al romperlo o colocarlo!

    // ════════════════════════════════════════════════════════════
    //  SETAS
    // ════════════════════════════════════════════════════════════
    //   MYSTICA_CROP, ONIRICA_CROP, CHAOS_CROP
    //   COMPOSTED_SUBSTRATE, ENRICHED_SUBSTRATE
}