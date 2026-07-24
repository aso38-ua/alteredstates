package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import sereneseasons.api.season.SeasonHelper;
import sereneseasons.api.season.Season;

public class OniricMushroomCropBlock extends CropBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D),
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D)
    };

    public OniricMushroomCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }

    @Override
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected ItemLike getBaseSeedId() { return ModItems.ONIRIC_MUSHROOM_SPORES.get(); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // 🧱 SUPERVIVENCIA: EXTREMADAMENTE EXIGENTE
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // ❌ Adiós al Blocks.MYCELIUM. Solo acepta tus sustratos custom.
        return state.is(ModBlocks.COMPOSTED_SUBSTRATE.get()) ||
                state.is(ModBlocks.ENRICHED_SUBSTRATE.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) return false;
        return level.getMaxLocalRawBrightness(pos) < 13;
    }

    // 🌿 CRECIMIENTO LENTO Y DELICADO
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentAge = state.getValue(AGE);
        if (currentAge < MAX_AGE) {

            BlockState soil = level.getBlockState(pos.below());

            // Es mucho más difícil de cultivar (25.0F vs los 15.0F de la mística)
            float baseGrowthChance = 25.0F;
            float multiplier = 1.0F; // Base en Composted

            // En el sustrato enriquecido crece el doble de rápido
            if (soil.is(ModBlocks.ENRICHED_SUBSTRATE.get())) multiplier = 2.0F;

            if (CompatManager.SERENE_SEASONS && level.canSeeSky(pos)) {
                Season season = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
                if (season == Season.WINTER || season == Season.SUMMER) return; // Odia los extremos
            }

            float finalChance = baseGrowthChance / multiplier;
            if (level.getMaxLocalRawBrightness(pos) >= 10) finalChance *= 15.0F; // Super sensible a la luz

            if (random.nextInt((int) finalChance + 1) == 0) {
                level.setBlock(pos, state.setValue(AGE, currentAge + 1), 2);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) == MAX_AGE) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                int calculatedQuality = calculateQuality(serverLevel, pos);

                ItemStack mushroom = new ItemStack(ModItems.ONIRIC_MUSHROOM_FRESH.get(), 1);
                mushroom.set(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), calculatedQuality);

                Block.popResource(level, pos, mushroom);

                if (level.random.nextFloat() < 0.20F) { // Menos drop de esporas (20%)
                    Block.popResource(level, pos, new ItemStack(ModItems.ONIRIC_MUSHROOM_SPORES.get(), 1));
                }

                level.playSound(null, pos, SoundEvents.MOOSHROOM_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private int calculateQuality(ServerLevel level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        int quality = 2; // NORMAL base para el Composted

        if (soil.is(ModBlocks.ENRICHED_SUBSTRATE.get())) {
            quality = 4; // PREMIUM instantáneo si le das el mejor suelo
        }

        if (level.getMaxLocalRawBrightness(pos) >= 8) {
            quality -= 2; // Castigo duro por luz
        }

        return Math.max(1, quality);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }
}