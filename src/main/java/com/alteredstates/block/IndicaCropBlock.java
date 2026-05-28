package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

// Importaciones dinámicas para evitar problemas si Serene Seasons no está instalado
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.api.season.Season;

public class IndicaCropBlock extends CropBlock {
    // 8 Sprites = 8 Fases (0, 1, 2, 3, 4, 5, 6, 7)
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public IndicaCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.INDICA_SEEDS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // REGLA DE CULTIVO: Requiere Podzol o Tierra Labrada
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected int getBonemealAgeIncrease(net.minecraft.world.level.Level level) {
        // Por defecto Vanilla hace: return Mth.nextInt(level.random, 2, 5);
        // Nosotros le decimos que solo crezca 1 fase por cada polvo de hueso.
        return 1;
    }

    // 🔪 COSECHA CON CLICK DERECHO (Estilo Farmer's Delight)
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        int age = state.getValue(getAgeProperty());

        // Si está en fase máxima (7)
        if (age == getMaxAge()) {
            // Suelta el loot como si la hubieran roto
            level.destroyBlock(pos, true, player);
            // Y la replanta instantáneamente en fase 0
            level.setBlock(pos, this.defaultBlockState(), 3);
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    // 🌿 SISTEMA DE CRECIMIENTO AVANZADO (Luz + Clima + Estaciones)
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;

        int currentAge = this.getAge(state);
        if (currentAge >= this.getMaxAge()) return;

        // 1. Chequeo de LUZ (Reglas definitivas del diseño)
        int lightLevel = level.getMaxLocalRawBrightness(pos.above());
        if (lightLevel < 8) {
            // < 8 → No crece (Aquí luego engancharemos el sistema de pérdida de calidad si pasa mucho tiempo)
            return;
        }

        // Calcular modificador por nivel de luz
        float lightModifier = 1.0F;
        if (lightLevel >= 8 && lightLevel <= 11) {
            lightModifier = 0.5F; // Crece lento
        } else if (lightLevel >= 12 && lightLevel <= 15) {
            lightModifier = 1.2F; // Crecimiento óptimo
        }

        // 2. Chequeo de ESTACIONES (Serene Seasons)
        float seasonModifier = 1.0F;
        if (CompatManager.SERENE_SEASONS) {
            Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
            Season season = subSeason.getSeason();

            if (season == Season.WINTER) {
                // Invierno → No crece. La planta hiberna
                return;
            } else if (season == Season.SUMMER) {
                seasonModifier = 1.2F; // Verano → Crecimiento +20%
            } else if (season == Season.AUTUMN) {
                seasonModifier = 0.6F; // Otoño → Crecimiento -40%
            }
        }

        // 3. Chequeo de LLUVIA DIRECTA
        float rainModifier = 1.0F;
        if (level.isRaining() && level.canSeeSky(pos.above())) {
            rainModifier = 0.8F; // Lluvia directa → Penalización del -20%
        }

        // Unimos todos los multiplicadores para calcular la probabilidad real de crecimiento
        float baseGrowthChance = getGrowthSpeed(this.defaultBlockState(), level, pos);
        float finalGrowthChance = baseGrowthChance * lightModifier * seasonModifier * rainModifier;

        // Mecánica estándar de Minecraft adaptada a nuestras probabilidades mutadas
        if (random.nextInt((int)(25.0F / finalGrowthChance) + 1) == 0) {
            level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
        }
    }
}