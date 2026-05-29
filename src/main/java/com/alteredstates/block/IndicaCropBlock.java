package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import sereneseasons.api.season.SeasonHelper;
import sereneseasons.api.season.Season;

public class IndicaCropBlock extends CropBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public IndicaCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected ItemLike getBaseSeedId() { return ModItems.INDICA_SEEDS.get(); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected int getBonemealAgeIncrease(net.minecraft.world.level.Level level) {
        return 1;
    }

    // 🔪 COSECHA CON CLICK DERECHO REUTILIZANDO EL SISTEMA DE LOOT DINÁMICO
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        int age = state.getValue(getAgeProperty());

        if (age == getMaxAge()) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                // Generamos los parámetros mínimos para que getDrops pueda leer el mundo
                net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY);

                // Soltar ítems calculados
                for (ItemStack drop : this.getDrops(state, builder)) {
                    Block.popResource(level, pos, drop);
                }

                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CROP_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, this.defaultBlockState().setValue(AGE, 0), 3);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    // 🔄 RECOLECCIÓN UNIFICADA (Para click derecho Y si rompen el bloque con la mano/herramienta)
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        int age = state.getValue(AGE);
        ServerLevel serverLevel = builder.getLevel();

        if (age == MAX_AGE) {
            BlockPos pos = BlockPos.containing(builder.getParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN));
            int calculatedQuality = 2; // Base: Normal

            if (pos != null) {
                int lightLevel = serverLevel.getMaxLocalRawBrightness(pos.above());
                boolean isRaining = serverLevel.isRainingAt(pos.above());
                boolean seasonBad = false;
                boolean seasonOptimal = false;

                if (CompatManager.SERENE_SEASONS) {
                    Season season = SeasonHelper.getSeasonState(serverLevel).getSubSeason().getSeason();
                    if (season == Season.WINTER || season == Season.AUTUMN) seasonBad = true;
                    else if (season == Season.SUMMER) seasonOptimal = true;
                }

                if (lightLevel < 8 || isRaining || seasonBad) calculatedQuality = 1; // Regular
                else if (lightLevel >= 12 && !isRaining && (seasonOptimal || !CompatManager.SERENE_SEASONS)) calculatedQuality = 3; // Buena
            }

            // 1. Cogollos Frescos [cite: 44]
            int budCount = serverLevel.random.nextInt(2) + 2;
            ItemStack buds = new ItemStack(ModItems.INDICA_BUDS_FRESH.get(), budCount);
            buds.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
            drops.add(buds);

            // 2. Semillas [cite: 44]
            int seedCount = serverLevel.random.nextInt(2) + 1;
            drops.add(new ItemStack(this.getBaseSeedId(), seedCount));

            // 3. 🌿 ¡NUEVO! Trimmings añadidos (Drop de 1 a 3 unidades según GDD)
            int trimmingCount = serverLevel.random.nextInt(3) + 1;
            drops.add(new ItemStack(ModItems.CANNABIS_TRIMMING.get(), trimmingCount));

        } else {
            drops.add(new ItemStack(this.getBaseSeedId(), 1));
        }
        return drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;

        int currentAge = this.getAge(state);
        if (currentAge >= this.getMaxAge()) return;

        int lightLevel = level.getMaxLocalRawBrightness(pos.above());

        // Modificador de luz adaptado: ya no detiene el crecimiento, solo va lentísimo
        float lightModifier = 1.0F;
        if (lightLevel < 8) {
            lightModifier = 0.2F; // ¡NUEVO! Sigue creciendo a oscuras, pero a paso de tortuga
        } else if (lightLevel >= 8 && lightLevel <= 11) {
            lightModifier = 0.5F;
        } else if (lightLevel >= 12 && lightLevel <= 15) {
            lightModifier = 1.2F;
        }

        float seasonModifier = 1.0F;
        if (CompatManager.SERENE_SEASONS) {
            Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
            Season season = subSeason.getSeason();

            if (season == Season.WINTER) {
                return; // Hibernación completa
            } else if (season == Season.SUMMER) {
                seasonModifier = 1.2F;
            } else if (season == Season.AUTUMN) {
                seasonModifier = 0.6F;
            }
        }

        float rainModifier = 1.0F;
        if (level.isRaining() && level.canSeeSky(pos.above())) {
            rainModifier = 0.8F;
        }

        float baseGrowthChance = getGrowthSpeed(this.defaultBlockState(), level, pos);
        float finalGrowthChance = baseGrowthChance * lightModifier * seasonModifier * rainModifier;

        if (random.nextInt((int)(25.0F / finalGrowthChance) + 1) == 0) {
            level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
        }
    }
}