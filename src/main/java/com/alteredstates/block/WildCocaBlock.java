package com.alteredstates.block;

import com.alteredstates.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

/** Coca salvaje (jungla): arbusto de 1 bloque. Al romperla da semillas y algo de hoja. */
public class WildCocaBlock extends BushBlock {

    public WildCocaBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        var random = builder.getLevel().random;
        drops.add(new ItemStack(ModItems.COCA_SEEDS.get(), random.nextInt(2) + 1));
        drops.add(new ItemStack(ModItems.COCA_LEAF.get(), random.nextInt(2) + 1));
        return drops;
    }
}
