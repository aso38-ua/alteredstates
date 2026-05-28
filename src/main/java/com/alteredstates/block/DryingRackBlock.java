package com.alteredstates.block;

import com.alteredstates.block.entity.DryingRackBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.Containers;
import org.jetbrains.annotations.Nullable;
import com.alteredstates.registry.ModItems;

public class DryingRackBlock extends BaseEntityBlock {
    // Definimos una forma plana que parezca un estante de pared o techo
    // Block.box(x_min, y_min, z_min, x_max, y_max, z_max)
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public DryingRackBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Hace caso a los archivos JSON del modelo del bloque
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof DryingRackBlockEntity blockEntity) {
            ItemStack itemInHand = player.getMainHandItem();

            // CASO A: El jugador tiene un cogollo fresco en la mano -> Intentamos colgarlo
            if (itemInHand.is(ModItems.INDICA_BUDS_FRESH.get())) {
                if (blockEntity.addItem(itemInHand)) {
                    if (!player.getAbilities().instabuild) {
                        itemInHand.shrink(1); // Consumimos uno si no está en creativo
                    }
                    level.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            // CASO B: El jugador tiene la mano vacía -> Intentamos sacar algo ya seco
            else if (itemInHand.isEmpty()) {
                ItemStack finishedItem = blockEntity.takeFinishedItem();
                if (!finishedItem.isEmpty()) {
                    if (!player.getInventory().add(finishedItem)) {
                        player.drop(finishedItem, false);
                    }
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    // Si rompen el secadero, tira lo que tenía colgado al suelo
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof com.alteredstates.block.entity.DryingRackBlockEntity tile) {

                // Bucle para tirar TODOS los cogollos que hubiera colgando
                for (net.minecraft.world.item.ItemStack stack : tile.getItems()) {
                    if (!stack.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }

                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        // Cambiado serverTick por tick al final de esta línea:
        return level.isClientSide ? null : createTickerHelper(type, com.alteredstates.registry.ModBlockEntities.DRYING_RACK.get(), com.alteredstates.block.entity.DryingRackBlockEntity::tick);
    }
}