package com.alteredstates.block;

import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class WildOniricMushroomBlock extends BushBlock {

    public WildOniricMushroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }

    // Permitimos que crezca en césped, tierra, podzol, micelio y tus sustratos
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) ||
                state.is(ModBlocks.COMPOSTED_SUBSTRATE.get()) || state.is(ModBlocks.ENRICHED_SUBSTRATE.get());
    }

    // Al romperla, suelta esporas y una seta fresca
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        // Entre 1 y 2 esporas
        drops.add(new ItemStack(ModItems.ONIRIC_MUSHROOM_SPORES.get(), builder.getLevel().random.nextInt(2) + 1));
        // Una seta onírica fresca de calidad base
        drops.add(new ItemStack(ModItems.ONIRIC_MUSHROOM_FRESH.get(), 1));
        return drops;
    }
}