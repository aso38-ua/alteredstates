package com.alteredstates.block;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

/**
 * Coca: 1 bloque, 8 fases (0-7). Solo se cosecha con TIJERAS en la fase 7
 * y rebrota a la fase REGROW_AGE (perenne, como la Sativa).
 * Romperla a mano solo devuelve semillas.
 */
public class CocaCropBlock extends CropBlock {
    public static final int MAX_AGE = 7;
    public static final int REGROW_AGE = 4;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public CocaCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override public int getMaxAge() { return MAX_AGE; }
    @Override protected ItemLike getBaseSeedId() { return ModItems.COCA_SEEDS.get(); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD);
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) { return 1; }

    // ✂️ Cosecha con tijeras (fase 7) -> vuelve a fase 4
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) == MAX_AGE && stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                int quality = calculateQuality(serverLevel, pos);
                ItemStack leaves = new ItemStack(ModItems.COCA_LEAF.get(), serverLevel.random.nextInt(3) + 3);
                leaves.set(ModDataComponentTypes.QUALITY.get(), quality);
                Block.popResource(level, pos, leaves);
                if (serverLevel.random.nextFloat() < 0.35F) {
                    Block.popResource(level, pos, new ItemStack(this.getBaseSeedId(), 1));
                }
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                level.setBlock(pos, state.setValue(AGE, REGROW_AGE), 3);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    // Romper a mano: solo semilla (la hoja exige tijeras)
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        ServerLevel serverLevel = builder.getLevel();
        int seeds = state.getValue(AGE) == MAX_AGE ? serverLevel.random.nextInt(2) + 1 : 1;
        drops.add(new ItemStack(this.getBaseSeedId(), seeds));
        return drops;
    }

    private int calculateQuality(ServerLevel level, BlockPos pos) {
        int quality = 2;
        int light = level.getMaxLocalRawBrightness(pos.above());
        boolean rain = level.isRainingAt(pos.above());
        boolean bad = false, optimal = false;
        if (CompatManager.SERENE_SEASONS) {
            Season s = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
            if (s == Season.WINTER || s == Season.AUTUMN) bad = true;
            else if (s == Season.SUMMER) optimal = true;
        }
        if (light < 8 || rain || bad) quality = 1;
        else if (light >= 12 && (optimal || !CompatManager.SERENE_SEASONS)) quality = 3;
        return quality;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        int age = this.getAge(state);
        if (age >= MAX_AGE) return;

        int light = level.getMaxLocalRawBrightness(pos.above());
        float lightMod = light < 8 ? 0.2F : (light <= 11 ? 0.5F : 1.2F);

        float seasonMod = 1.0F;
        if (CompatManager.SERENE_SEASONS) {
            Season s = SeasonHelper.getSeasonState(level).getSubSeason().getSeason();
            if (s == Season.WINTER) return;
            if (s == Season.SUMMER) seasonMod = 1.2F;
            else if (s == Season.AUTUMN) seasonMod = 0.6F;
        }
        float rainMod = (level.isRaining() && level.canSeeSky(pos.above())) ? 0.8F : 1.0F;

        float chance = getGrowthSpeed(this.defaultBlockState(), level, pos) * lightMod * seasonMod * rainMod;
        if (random.nextInt((int) (25.0F / chance) + 1) == 0) {
            level.setBlock(pos, this.getStateForAge(age + 1), 2);
        }
    }
}
