package com.alteredstates.block;

import com.alteredstates.block.entity.LabFurnaceBlockEntity;
import com.alteredstates.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LabFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LabFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return null; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LabFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.LAB_FURNACE.get(), LabFurnaceBlockEntity::tick);
    }

    // Con ítem: pasta de coca (agachado = todo el stack) o combustible
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LabFurnaceBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);

        if (be.tryInsertInput(stack, player.isShiftKeyDown(), !player.hasInfiniteMaterials())) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        int burn = stack.getBurnTime(RecipeType.SMELTING);
        if (burn > 0 && be.canTakeFuel(burn)) {
            be.addFuel(burn);
            if (!player.hasInfiniteMaterials()) {
                ItemStack remainder = stack.getCraftingRemainingItem();
                stack.shrink(1);
                if (!remainder.isEmpty()) {
                    if (stack.isEmpty()) player.setItemInHand(hand, remainder);
                    else if (!player.getInventory().add(remainder)) player.drop(remainder, false);
                }
            }
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.2F);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Mano vacía: recoger cocaína; agachado, recuperar la pasta sin cocinar
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof LabFurnaceBlockEntity be) || be.isEmpty()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            ItemStack taken = player.isShiftKeyDown() ? be.takeInput() : be.takeOutput();
            if (taken.isEmpty()) taken = player.isShiftKeyDown() ? be.takeOutput() : be.takeInput();
            if (!taken.isEmpty()) {
                if (!player.getInventory().add(taken)) player.drop(taken, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof LabFurnaceBlockEntity be) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getInput());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getOutput());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    // 🔥 Llamas por delante, humo por la chimenea (esquina trasera derecha del modelo)
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);

        if (random.nextInt(4) == 0) {
            double fx = pos.getX() + 0.5D + facing.getStepX() * 0.52D + (random.nextDouble() - 0.5D) * 0.4D * Math.abs(facing.getStepZ());
            double fz = pos.getZ() + 0.5D + facing.getStepZ() * 0.52D + (random.nextDouble() - 0.5D) * 0.4D * Math.abs(facing.getStepX());
            level.addParticle(ParticleTypes.FLAME, fx, pos.getY() + 0.2D + random.nextDouble() * 0.2D, fz, 0, 0.005D, 0);
        }

        // posición local de la chimenea respecto al centro (+0.25, +0.25) rotada según FACING
        double lx = 0.25D, lz = 0.25D;
        int turns = (((int) facing.toYRot() + 180) % 360) / 90;
        for (int i = 0; i < turns; i++) { double nx = -lz; lz = lx; lx = nx; }
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D + lx, pos.getY() + 1.0D, pos.getZ() + 0.5D + lz, 0, 0.04D, 0);
        }
        if (random.nextInt(30) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F, false);
        }
    }
}
