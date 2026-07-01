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
import net.minecraft.world.level.*;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class TobaccoCropBlock extends CropBlock {
    // 9 Fases (0 a 8)
    public static final int MAX_AGE = 8;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    // Propiedad que define si es la mitad de abajo o la de arriba
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public TobaccoCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected ItemLike getBaseSeedId() { return ModItems.TOBACCO_SEEDS.get(); }

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

    // 🌾 COSECHA AL ROMPER (Mecánica estilo Trigo + Soporte de Tijeras)
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack tool) {
        // Ejecutamos la lógica base primero (para estadísticas del jugador y compatibilidades)
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            int age = state.getValue(AGE);

            // Solo procesamos los drops personalizados si la planta está madura (Fase 8)
            if (age == MAX_AGE) {
                // Aseguramos la posición de la parte superior para calcular la calidad de forma uniforme
                BlockPos upperPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos;
                int calculatedQuality = calculateQuality(serverLevel, upperPos);

                // Comprobamos si el jugador usó tijeras (cizallas)
                boolean isShears = tool.getItem() instanceof ShearsItem;

                // Si usa tijeras, le damos un bono (+1 hoja garantizada de cada tipo, por ejemplo)
                int bonus = isShears ? 1 : 0;

                int capoteCount = serverLevel.random.nextInt(2) + bonus;
                int capaCount = serverLevel.random.nextInt(2) + bonus;
                int tripaCount = serverLevel.random.nextInt(2) + bonus;

                // Determinamos el punto de spawn de los items (siempre abajo para que no floten)
                BlockPos dropPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();

                // Soltamos las hojas con su respectiva calidad
                if (capoteCount > 0) {
                    ItemStack capote = new ItemStack(ModItems.CAPOTE_FRESH.get(), capoteCount);
                    capote.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                    Block.popResource(level, dropPos, capote);
                }

                if (capaCount > 0) {
                    ItemStack capa = new ItemStack(ModItems.CAPA_FRESH.get(), capaCount);
                    capa.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                    Block.popResource(level, dropPos, capa);
                }

                if (tripaCount > 0) {
                    ItemStack tripa = new ItemStack(ModItems.TRIPA_FRESH.get(), tripaCount);
                    tripa.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                    Block.popResource(level, dropPos, tripa);
                }

                // Soltamos las semillas de tabaco (entre 1 y 3, simulando al trigo vanilla)
                int seedCount = 1 + serverLevel.random.nextInt(3);
                Block.popResource(level, dropPos, new ItemStack(this.getBaseSeedId(), seedCount));

                // Si se usaron tijeras, les aplicamos daño por desgaste (1 de durabilidad)
                if (isShears) {
                    tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                }
            }
        }
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
            growthPenalty = 0.9F; // Un 10% más lento en la última fase
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