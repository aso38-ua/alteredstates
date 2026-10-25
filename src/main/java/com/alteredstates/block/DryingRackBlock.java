package com.alteredstates.block;

import com.alteredstates.block.entity.DryingRackBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.Containers;
import org.jetbrains.annotations.Nullable;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.ItemInteractionResult;

public class DryingRackBlock extends BaseEntityBlock {
    // Definimos una forma plana que parezca un estante de pared o techo
    // Block.box(x_min, y_min, z_min, x_max, y_max, z_max)
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public DryingRackBlock(Properties properties) {
        super(properties);
        // estado por defecto mirando al norte
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;



    //registrar la propiedad
    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    //asignar dirección al colocar ────────────────────
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
                                              Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // 1) Producto fresco en mano → colgarlo
        if (stack.getItem() instanceof com.alteredstates.item.IDryable) {
            if (!be.hasSpace())
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!level.isClientSide) {
                be.addItem(stack);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 2) El resultado seco de lo que hay colgado → recoger UNO
        if (be.hasFinishedOfType(stack.getItem())) {
            if (!level.isClientSide) {
                give(player, be.takeFinishedOfType(stack.getItem()));
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 3) Cualquier otra cosa en mano → no hace nada
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        // Solo con la mano principal vacía (y sin otro secable en la secundaria)
        if (!player.getMainHandItem().isEmpty()
                || player.getOffhandItem().getItem() instanceof com.alteredstates.item.IDryable)
            return InteractionResult.PASS;

        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity be) || !be.hasAnyItem())
            return InteractionResult.PASS;

        if (!level.isClientSide) {
            // Primero lo ya seco; si no hay, deshace el último colgado
            ItemStack out = be.takeFinishedItem();
            if (out.isEmpty()) out = be.takeLastItem();
            give(player, out);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void give(Player player, ItemStack out) {
        if (out.isEmpty()) return;
        if (!player.getInventory().add(out)) player.drop(out, false);
    }
}