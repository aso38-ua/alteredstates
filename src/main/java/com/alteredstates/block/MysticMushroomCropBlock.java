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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Importaciones de Serene Seasons
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.api.season.Season;

public class MysticMushroomCropBlock extends CropBlock {
    // 4 Fases (0, 1, 2, 3)
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    // Hitboxes que van creciendo con la seta
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D),
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D)
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
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected ItemLike getBaseSeedId() { return ModItems.MYSTIC_MUSHROOM_SPORES.get(); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // 🧱 SUPERVIVENCIA: Sustratos válidos
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.MYCELIUM) ||
                state.is(ModBlocks.COMPOSTED_SUBSTRATE.get()) ||
                state.is(ModBlocks.ENRICHED_SUBSTRATE.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) return false;

        // Exige oscuridad (Luz menor a 13)
        return level.getMaxLocalRawBrightness(pos) < 13;
    }

    // 🌿 CRECIMIENTO + CLIMA + SUSTRATOS
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentAge = state.getValue(AGE);
        if (currentAge < MAX_AGE) {

            BlockState soil = level.getBlockState(pos.below());

            // Usamos un número fijo propio en lugar de la fórmula del trigo de Minecraft
            // Cuanto MÁS ALTO sea este número, MÁS LENTO crece por defecto.
            float baseGrowthChance = 15.0F;
            float multiplier = 1.0F; // Base en Micelio

            if (soil.is(ModBlocks.COMPOSTED_SUBSTRATE.get())) multiplier = 1.5F;
            else if (soil.is(ModBlocks.ENRICHED_SUBSTRATE.get())) multiplier = 2.5F;

            // Integración con Serene Seasons (Solo si está bajo el cielo)
            if (CompatManager.SERENE_SEASONS && level.canSeeSky(pos)) {
                Season season = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
                if (season == Season.WINTER) return;
                if (season == Season.SUMMER && !level.isRainingAt(pos)) {
                    multiplier *= 0.2F;
                }
            }

            // Aplicamos multiplicador
            float finalChance = baseGrowthChance / multiplier;

            if (level.getMaxLocalRawBrightness(pos) >= 10) finalChance *= 10.0F; // Penalización masiva por luz

            // Intentamos crecer
            if (random.nextInt((int) finalChance + 1) == 0) {
                level.setBlock(pos, state.setValue(AGE, currentAge + 1), 2);
            }
        }
    }

    // ✂️ COSECHA CON CALIDAD
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) == MAX_AGE) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

                // 1. Calculamos la calidad en base a los cuidados
                int calculatedQuality = calculateQuality(serverLevel, pos);

                // 2. Creamos el ítem de la seta y le inyectamos la calidad
                ItemStack mushroom = new ItemStack(ModItems.MYSTIC_MUSHROOM_FRESH.get(), 1);
                mushroom.set(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), calculatedQuality);

                // 3. Soltamos la seta en el mundo
                Block.popResource(level, pos, mushroom);

                // 4. Drop de esporas (30% de probabilidad como extra)
                if (level.random.nextFloat() < 0.30F) {
                    Block.popResource(level, pos, new ItemStack(ModItems.MYSTIC_MUSHROOM_SPORES.get(), 1));
                }

                level.playSound(null, pos, SoundEvents.MOOSHROOM_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);

                // El hongo sigue vivo bajo la tierra, vuelve a la fase 0
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    // 🔬 EL CÁLCULO DE CALIDAD
    private int calculateQuality(ServerLevel level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        int quality = 2; // NORMAL base para el Micelio

        // Bonificaciones por buen sustrato
        if (soil.is(ModBlocks.COMPOSTED_SUBSTRATE.get())) {
            quality = 3; // BUENA
        } else if (soil.is(ModBlocks.ENRICHED_SUBSTRATE.get())) {
            quality = 4; // PREMIUM
        }

        // Penalización por mal cuidado: La luz las estresa.
        // Si el nivel de luz es mayor o igual a 8, pierden un nivel de calidad.
        if (level.getMaxLocalRawBrightness(pos) >= 8) {
            quality -= 1;
        }

        // Garantizamos que si lo haces fatal, lo mínimo que te da es REGULAR (1)
        return Math.max(1, quality);
    }

    // ❌ DESACTIVAR POLVO DE HUESO (Las setas mágicas necesitan su tiempo)
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }
}