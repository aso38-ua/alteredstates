package com.alteredstates.block;

import com.alteredstates.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.List;

public class WildSativaBlock extends TallFlowerBlock {

    public WildSativaBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();

        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            drops.add(new ItemStack(ModItems.SATIVA_SEEDS.get(), builder.getLevel().random.nextInt(2) + 1));
            drops.add(new ItemStack(ModItems.SATIVA_BUDS_FRESH.get(), 1));
        }
        return drops;
    }
}