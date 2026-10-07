package com.alteredstates.block;

import com.alteredstates.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class WildTobaccoBlock extends BushBlock {

    public WildTobaccoBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }

    // Permitimos que crezca en césped, tierra, podzol, tierra arada y variantes de tierra
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    // Al romperla en el mundo salvaje, nos da sus semillas y hojas frescas
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        // Nos asegura conseguir entre 1 y 2 semillas para empezar el cultivo doméstico
        drops.add(new ItemStack(ModItems.TOBACCO_SEEDS.get(), builder.getLevel().random.nextInt(2) + 1));

        // Hojas frescas de tabaco (Capa, Capote o Tripa)
        Item[] leafTypes = new Item[] {
                ModItems.CAPOTE_FRESH.get(),
                ModItems.CAPA_FRESH.get(),
                ModItems.TRIPA_FRESH.get()
        };

        // Suelta entre 1 y 2 hojas frescas variadas
        int leafCount = builder.getLevel().random.nextInt(2) + 1;
        for (int i = 0; i < leafCount; i++) {
            Item leaf = leafTypes[builder.getLevel().random.nextInt(leafTypes.length)];
            drops.add(new ItemStack(leaf, 1));
        }

        return drops;
    }
}