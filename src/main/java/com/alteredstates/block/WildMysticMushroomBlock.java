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

public class WildMysticMushroomBlock extends BushBlock {

    public WildMysticMushroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }

    // Permitimos que crezca en césped, podzol, tierra estéril, micelio y barro
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.PODZOL) ||
                state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.MYCELIUM) ||
                state.is(Blocks.MUD);
    }

    // Al romperla en el mundo salvaje, da sus esporas y una seta fresca
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        // Nos asegura conseguir entre 1 y 2 esporas para empezar el cultivo doméstico
        drops.add(new ItemStack(ModItems.MYSTIC_MUSHROOM_SPORES.get(), builder.getLevel().random.nextInt(2) + 1));
        // Una seta fresca
        drops.add(new ItemStack(ModItems.MYSTIC_MUSHROOM_FRESH.get(), 1));
        return drops;
    }
}