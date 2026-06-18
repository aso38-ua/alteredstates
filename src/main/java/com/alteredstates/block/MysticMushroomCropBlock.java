package com.alteredstates.block;

import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MysticMushroomCropBlock extends CropBlock {
    // 4 Fases (0, 1, 2, 3)
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    // Formas físicas (hitboxes) para cada fase para que crezca visualmente
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D), // Fase 0: Esporas pequeñitas
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D), // Fase 1: Botones
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D), // Fase 2: Seta joven
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D) // Fase 3: Seta adulta
    };

    public MysticMushroomCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    /*@Override
    protected ItemLike getBaseSeedId() {
        // Asumimos que tienes un ítem de esporas registrado
        return ModItems.MYSTIC_MUSHROOM_SPORES.get();
    }*/

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // 🧱 SUPERVIVENCIA: Solo en bloques específicos y a oscuras
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.MYCELIUM) ||
                state.is(ModBlocks.COMPOSTED_SUBSTRATE.get()) ||
                state.is(ModBlocks.ENRICHED_SUBSTRATE.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Primero comprueba si el bloque de abajo es válido
        if (!super.canSurvive(state, level, pos)) {
            return false;
        }
        // Segundo: Exige oscuridad. Si la luz es mayor a 12, la seta "muere" o no se puede plantar.
        // Las setas místicas son algo permisivas, pero no toleran la luz del sol directa.
        return level.getMaxLocalRawBrightness(pos) < 13;
    }

    // 🌿 CRECIMIENTO CON BOOST DE SUSTRATO
    /*@Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentAge = state.getValue(AGE);
        if (currentAge < MAX_AGE) {

            // 1. Miramos el bloque de abajo
            BlockState soil = level.getBlockState(pos.below());
            float growthSpeed = getGrowthSpeed(this, level, pos);

            // 2. Aplicamos multiplicadores según el sustrato (La magia de los Tiers)
            float multiplier = 1.0F; // Base (Micelio)

            if (soil.is(ModBlocks.COMPOSTED_SUBSTRATE.get())) {
                multiplier = 1.5F; // 50% más rápido en Compostado
            } else if (soil.is(ModBlocks.ENRICHED_SUBSTRATE.get())) {
                multiplier = 2.5F; // 150% más rápido en Enriquecido (¡Vuela!)
            }

            float finalChance = growthSpeed * multiplier;

            // 3. Comprobamos la oscuridad (si le da demasiada luz, el crecimiento se frena en seco)
            if (level.getMaxLocalRawBrightness(pos) >= 10) {
                finalChance *= 0.1F; // Penalización brutal por luz
            }

            // 4. Intentamos crecer
            if (random.nextInt((int)(25.0F / finalChance) + 1) == 0) {
                level.setBlock(pos, state.setValue(AGE, currentAge + 1), 2);
            }
        }
    }*/
}