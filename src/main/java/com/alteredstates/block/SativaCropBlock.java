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

        if (age == MAX_AGE && stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                // 1. Drops
                int calculatedQuality = calculateQuality(serverLevel, pos);
                int budCount = serverLevel.random.nextInt(3) + 2;
                ItemStack buds = new ItemStack(ModItems.SATIVA_BUDS_FRESH.get(), budCount);
                buds.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);

                Block.popResource(level, pos, buds);
                Block.popResource(level, pos, new ItemStack(ModItems.CANNABIS_TRIMMING.get(), serverLevel.random.nextInt(2) + 1));

                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                // 2. REGRESIÓN SEGURA A FASE 7 (Sincronización total)
                BlockPos lowerPos = (state.getValue(HALF) == DoubleBlockHalf.UPPER) ? pos.below() : pos;
                BlockPos upperPos = lowerPos.above();

                // Bajamos edad a 7 en ambos bloques simultáneamente
                level.setBlock(lowerPos, state.setValue(AGE, 7).setValue(HALF, DoubleBlockHalf.LOWER), 3);
                level.setBlock(upperPos, state.setValue(AGE, 7).setValue(HALF, DoubleBlockHalf.UPPER), 3);
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
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;

        int currentAge = state.getValue(AGE);
        // Eliminamos el 'return' si currentAge >= MAX_AGE, porque queremos permitir
        // que el código llegue a la lógica de actualización si es necesario
        if (currentAge >= MAX_AGE) return;

        float growthPenalty = (currentAge == 7) ? 0.1F : ((currentAge >= 5) ? 0.5F : 1.0F);
        int lightLevel = level.getMaxLocalRawBrightness(pos.above());
        float lightModifier = (lightLevel < 8) ? 0.2F : ((lightLevel <= 11) ? 0.5F : 1.2F);
        float finalChance = getGrowthSpeed(this.defaultBlockState(), level, pos) * lightModifier * growthPenalty;

        if (random.nextInt((int)(25.0F / finalChance) + 1) == 0) {
            int nextAge = currentAge + 1;
            BlockPos topPos = pos.above();

            if (nextAge >= 5) {
                // Si la parte de arriba no existe, la creamos
                if (level.isEmptyBlock(topPos)) {
                    level.setBlock(pos, state.setValue(AGE, nextAge), 2);
                    level.setBlock(topPos, state.setValue(AGE, nextAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
                } else {
                    // Si ya existe (cosechada), actualizamos ambas partes
                    level.setBlock(pos, state.setValue(AGE, nextAge), 2);
                    if (level.getBlockState(topPos).is(this)) {
                        level.setBlock(topPos, level.getBlockState(topPos).setValue(AGE, nextAge), 2);
                    }
                }
            } else {
                level.setBlock(pos, state.setValue(AGE, nextAge), 2);
            }
        }
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // 1. Delegación si clicamos arriba
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos basePos = pos.below();
            BlockState baseState = level.getBlockState(basePos);
            if (baseState.is(this)) {
                performBonemeal(level, random, basePos, baseState);
            }
            return;
        }

        // 2. Lógica de crecimiento (Base)
        int currentAge = state.getValue(AGE);
        if (currentAge < MAX_AGE) {
            int nextAge = currentAge + 1;

            // Si es fase 5 o más, necesitamos actualizar ambos bloques
            if (nextAge >= 5) {
                BlockPos topPos = pos.above();

                // Actualizamos la base
                level.setBlock(pos, state.setValue(AGE, nextAge), 2);

                // SI EL BLOQUE DE ARRIBA YA EXISTE, actualizamos su edad también
                if (level.getBlockState(topPos).is(this)) {
                    level.setBlock(topPos, level.getBlockState(topPos).setValue(AGE, nextAge), 2);
                }
                // SI NO EXISTE (acaba de llegar a fase 5), lo creamos
                else if (level.isEmptyBlock(topPos)) {
                    level.setBlock(topPos, state.setValue(AGE, nextAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
                }
            } else {
                // Crecimiento normal (fase < 5)
                level.setBlock(pos, state.setValue(AGE, nextAge), 2);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        // Si la planta es UPPER, miramos la base para ver si tiene edad máxima
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return level.getBlockState(pos.below()).getValue(AGE) < 8;
        }
        // Si es BASE, miramos su propia edad
        return state.getValue(AGE) < 8;
    }
}