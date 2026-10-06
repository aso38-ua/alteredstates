package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.ArrayList;
import java.util.List;

public class TobaccoCropBlock extends CropBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public TobaccoCropBlock(Properties properties) {
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
        return ModItems.TOBACCO_SEEDS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return 1;
    }

    // 🌾 DROPS AL ROMPER EL CULTIVO (Mecánica vanilla: requiere romper el bloque para cosechar)
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        int age = state.getValue(AGE);
        ServerLevel serverLevel = builder.getLevel();

        if (age == MAX_AGE) {
            BlockPos pos = BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN));
            int calculatedQuality = calculateQuality(serverLevel, pos);

            // Comprobamos si la herramienta utilizada fueron cizallas / tijeras
            ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
            boolean isShears = tool != null && tool.getItem() instanceof ShearsItem;
            int bonus = isShears ? 1 : 0;

            // 1. Capote Fresco
            int capoteCount = serverLevel.random.nextInt(2) + bonus;
            if (capoteCount > 0) {
                ItemStack capote = new ItemStack(ModItems.CAPOTE_FRESH.get(), capoteCount);
                capote.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                drops.add(capote);
            }

            // 2. Capa Fresca
            int capaCount = serverLevel.random.nextInt(2) + bonus;
            if (capaCount > 0) {
                ItemStack capa = new ItemStack(ModItems.CAPA_FRESH.get(), capaCount);
                capa.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                drops.add(capa);
            }

            // 3. Tripa Fresca
            int tripaCount = serverLevel.random.nextInt(2) + bonus;
            if (tripaCount > 0) {
                ItemStack tripa = new ItemStack(ModItems.TRIPA_FRESH.get(), tripaCount);
                tripa.set(ModDataComponentTypes.QUALITY.get(), calculatedQuality);
                drops.add(tripa);
            }

            // 4. Semillas de tabaco (entre 1 y 3 cuando está madura)
            int seedCount = 1 + serverLevel.random.nextInt(3);
            drops.add(new ItemStack(this.getBaseSeedId(), seedCount));

        } else {
            // Si la planta no está madura, solo suelta 1 semilla
            drops.add(new ItemStack(this.getBaseSeedId(), 1));
        }
        return drops;
    }

    // ✂️ DESGASTE DE TIJERAS AL COSECHAR
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (!level.isClientSide && !player.isCreative() && state.getValue(AGE) == MAX_AGE && tool.getItem() instanceof ShearsItem) {
            tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
        }
    }

    private int calculateQuality(ServerLevel level, BlockPos pos) {
        int calculatedQuality = 2; // Normal (Base)
        if (pos != null) {
            int lightLevel = level.getMaxLocalRawBrightness(pos.above());
            boolean isRaining = level.isRainingAt(pos.above());
            boolean seasonBad = false;
            boolean seasonOptimal = false;

            if (CompatManager.SERENE_SEASONS) {
                Season season = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
                if (season == Season.WINTER || season == Season.AUTUMN) seasonBad = true;
                else if (season == Season.SUMMER) seasonOptimal = true;
            }

            if (lightLevel < 8 || isRaining || seasonBad) {
                calculatedQuality = 1; // Regular
            } else if (lightLevel >= 12 && !isRaining && (seasonOptimal || !CompatManager.SERENE_SEASONS)) {
                calculatedQuality = 3; // Buena
            }
        }
        return calculatedQuality;
    }

    // 🌿 CRECIMIENTO LÓGICO
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;

        int currentAge = this.getAge(state);
        if (currentAge >= this.getMaxAge()) return;

        int lightLevel = level.getMaxLocalRawBrightness(pos.above());

        // Modificador de luz: a oscuras sigue creciendo pero muy lento
        float lightModifier = 1.0F;
        if (lightLevel < 8) {
            lightModifier = 0.2F;
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
                return; // Hibernación completa en invierno
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