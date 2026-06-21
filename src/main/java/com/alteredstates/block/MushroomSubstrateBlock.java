package com.alteredstates.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MushroomSubstrateBlock extends Block {

    private final boolean isEnriched;

    public MushroomSubstrateBlock(Properties properties, boolean isEnriched) {
        super(properties);
        this.isEnriched = isEnriched;
    }

    // 🧱 PRESETS DE PROPIEDADES PARA EL REGISTRO
    public static Properties getCompostedProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.6F)
                .sound(SoundType.MUD); // Suena a barro húmedo
    }

    public static Properties getEnrichedProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.8F)
                .sound(SoundType.SLIME_BLOCK); // Suena viscoso y alienígena
    }

    // ✨ EFECTOS VISUALES (Partículas para el "laboratorio")
    @Override
    public void animateTick(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.isEnriched) {
            // Probabilidad baja para no saturar la pantalla de partículas
            if (random.nextInt(10) == 0) {
                double d0 = (double)pos.getX() + random.nextDouble();
                double d1 = (double)pos.getY() + 1.05D; // Justo por encima del bloque
                double d2 = (double)pos.getZ() + random.nextDouble();

                // Emite partículas del portal del Nether (o podrías usar las de esporas de la 1.20 si prefieres)
                level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}