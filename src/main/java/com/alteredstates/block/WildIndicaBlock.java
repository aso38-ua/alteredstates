package com.alteredstates.block;

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

public class WildIndicaBlock extends BushBlock {

    public WildIndicaBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }

    // Permitimos que crezca en césped, tierra, podzol o tierra arada
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND);
    }

    // Al romperla en el mundo salvaje, nos da sus semillas y cogollos base
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        // Nos asegura conseguir entre 1 y 2 semillas para empezar el cultivo doméstico
        drops.add(new ItemStack(ModItems.INDICA_SEEDS.get(), builder.getLevel().random.nextInt(2) + 1));
        // Un cogollo básico de regalo
        drops.add(new ItemStack(ModItems.INDICA_BUDS_FRESH.get(), 1));
        return drops;
    }
}