package com.alteredstates.block;

import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WildSativaBlock extends TallFlowerBlock {

    public WildSativaBlock(Properties properties) {
        super(properties);
    }

    // 🛡️ MÉTODO SEGURO: Interceptamos cuando el jugador rompe el bloque (da igual la mitad)
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            // Solo soltamos botín si el jugador NO está en modo creativo
            if (!player.isCreative()) {
                // Preparamos el botín
                int seedCount = level.random.nextInt(2) + 1;
                ItemStack seeds = new ItemStack(ModItems.SATIVA_SEEDS.get(), seedCount);
                ItemStack bud = new ItemStack(ModItems.SATIVA_BUDS_FRESH.get(), 1);

                // Soltamos el botín físicamente en el mundo
                Block.popResource(level, pos, seeds);
                Block.popResource(level, pos, bud);
            }

            // Lógica Vanilla para destruir la otra mitad del bloque doble
            DoubleBlockHalf half = state.getValue(HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);
                if (lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    // Rompemos la parte de abajo de forma silenciosa para que no vuelva a generar botín
                    level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, lowerPos, Block.getId(lowerState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // 🗑️ Limpiamos los drops por defecto porque ya los hemos soltado manualmente en playerWillDestroy.
    // Esto asegura que NUNCA suelte botín doble, sin importar qué mod o explosión rompa el bloque.
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }
}