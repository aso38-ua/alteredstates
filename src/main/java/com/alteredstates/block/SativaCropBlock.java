package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.api.season.Season;

public class SativaCropBlock extends CropBlock {
    // 9 Fases (0 a 8)
    public static final int MAX_AGE = 8;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 8);
    // Propiedad que define si es la mitad de abajo o la de arriba
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public SativaCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected ItemLike getBaseSeedId() { return ModItems.SATIVA_SEEDS.get(); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    // 🧱 SUPERVIVENCIA DEL BLOQUE (La sincronización de las mitades)
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            // La parte de arriba solo sobrevive si debajo está la de abajo con la misma edad
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE).equals(state.getValue(AGE));
        }
        // La parte de abajo requiere tierra (Podzol o Farmland)
        return super.canSurvive(state, level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(this.getAgeProperty());

        // Si la edad es 8, forzamos al juego a usar la forma física de la fase 7
        // para que no intente buscar en el índice 8 del array original de Minecraft
        if (age > 7) {
            return super.getShape(state.setValue(this.getAgeProperty(), 7), level, pos, context);
        }

        return super.getShape(state, level, pos, context);
    }

    // Si rompen una mitad, se destruye la otra
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
            return facingState.is(this) && facingState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    // ✂️ COSECHA CON TIJERAS (Mecánica Perenne)
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int age = state.getValue(AGE);

        // Solo actuamos si es la fase máxima (8) y tenemos tijeras
        if (age == MAX_AGE && stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

                // 1. Lógica de drops (Calidad + Cogollos)
                int calculatedQuality = calculateQuality(serverLevel, pos);
                int budCount = serverLevel.random.nextInt(3) + 2;

                ItemStack buds = new ItemStack(ModItems.SATIVA_BUDS_FRESH.get(), budCount);
                buds.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);

                Block.popResource(level, pos, buds);
                Block.popResource(level, pos, new ItemStack(ModItems.CANNABIS_TRIMMING.get(), serverLevel.random.nextInt(2) + 1));

                // 2. Efectos visuales y de sonido
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                // 3. REGRESIÓN A FASE 7 (Doble bloque sincronizado)
                // Buscamos cuál es la posición de abajo (LOWER)
                BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
                BlockPos upperPos = lowerPos.above();

                // Actualizamos la parte de abajo a Edad 7
                level.setBlock(lowerPos, this.defaultBlockState()
                        .setValue(AGE, 7)
                        .setValue(HALF, DoubleBlockHalf.LOWER), 3);

                // Actualizamos la parte de arriba a Edad 7
                level.setBlock(upperPos, this.defaultBlockState()
                        .setValue(AGE, 7)
                        .setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private int calculateQuality(ServerLevel level, BlockPos pos) {
        int calculatedQuality = 2; // Normal (Aqua)
        int lightLevel = level.getMaxLocalRawBrightness(pos.above());
        boolean isRaining = level.isRainingAt(pos.above());
        boolean seasonBad = false;
        boolean seasonOptimal = false;

        if (CompatManager.SERENE_SEASONS) {
            Season season = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
            if (season == Season.WINTER || season == Season.AUTUMN) seasonBad = true;
            else if (season == Season.SUMMER) seasonOptimal = true;
        }

        if (lightLevel < 8 || isRaining || seasonBad) calculatedQuality = 1; // Regular
        else if (lightLevel >= 12 && !isRaining && (seasonOptimal || !CompatManager.SERENE_SEASONS)) calculatedQuality = 3; // Buena
        return calculatedQuality;
    }

    // 🌿 CRECIMIENTO LÓGICO
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Solo la parte de abajo ejecuta el crecimiento para evitar que crezca el doble de rápido
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;

        int currentAge = state.getValue(AGE);
        if (currentAge >= MAX_AGE) return;

        // Ralentizamos muchísimo el crecimiento de la fase 7 a la 8 (Floración final)
        float growthPenalty = 1.0F;
        if (currentAge == 7) {
            growthPenalty = 0.05F; // Un 95% más lento en la última fase
        } else if (currentAge >= 5) {
            growthPenalty = 0.5F;  // Un 50% más lento para crecer de la 5 a la 7
        }

        // Misma lógica de luz que la Indica
        int lightLevel = level.getMaxLocalRawBrightness(pos.above());
        float lightModifier = (lightLevel < 8) ? 0.2F : ((lightLevel <= 11) ? 0.5F : 1.2F);

        float baseChance = getGrowthSpeed(this.defaultBlockState(), level, pos);
        float finalChance = baseChance * lightModifier * growthPenalty;

        if (random.nextInt((int)(25.0F / finalChance) + 1) == 0) {
            int nextAge = currentAge + 1;

            // 🚀 EL ESTIRÓN (Al pasar a la Fase 5, necesita que haya aire encima)
            if (nextAge == 5) {
                if (level.isEmptyBlock(pos.above())) {
                    level.setBlock(pos, state.setValue(AGE, nextAge), 2);
                    level.setBlock(pos.above(), state.setValue(AGE, nextAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
                }
            }
            // Si ya es alta, actualizamos ambas partes simultáneamente
            else if (nextAge > 5) {
                level.setBlock(pos, state.setValue(AGE, nextAge), 2);
                level.setBlock(pos.above(), state.setValue(AGE, nextAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
            }
            // Crecimiento normal en 1 bloque
            else {
                level.setBlock(pos, state.setValue(AGE, nextAge), 2);
            }
        }
    }
}