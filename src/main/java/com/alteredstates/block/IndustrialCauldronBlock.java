package com.alteredstates.block;

import com.alteredstates.block.entity.IndustrialCauldronBlockEntity;
import com.alteredstates.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IndustrialCauldronBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public IndustrialCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return null; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
        return new IndustrialCauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.INDUSTRIAL_CAULDRON.get(), IndustrialCauldronBlockEntity::tick);
    }

    // Con ítem en la mano: cubos (agua / gasolina) o ingredientes
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof IndustrialCauldronBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 1) Cubos con fluido
        if (stack.getItem() instanceof BucketItem) {
            Optional<FluidStack> contained = FluidUtil.getFluidContained(stack);
            if (contained.isPresent() && !contained.get().isEmpty()) {
                FluidStack fluid = contained.get();
                if (be.fillInternal(fluid, false) >= fluid.getAmount()) {
                    if (!level.isClientSide) {
                        be.fillInternal(fluid, true);
                        if (!player.hasInfiniteMaterials()) {
                            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
                        }
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.CONSUME;
            }
        }

        // 2) Ingredientes (agachado = todo el stack)
        if (!level.isClientSide) {
            if (be.tryInsert(stack, player.isShiftKeyDown(), !player.hasInfiniteMaterials())) {
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4F, 1.4F);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.sidedSuccess(true);
    }

    // Mano vacía: recoger resultado -> sacar último ingrediente -> (agachado, vacío) tirar líquido
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof IndustrialCauldronBlockEntity be)) return InteractionResult.PASS;
        if (be.isEmpty()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            ItemStack taken = player.isShiftKeyDown() ? ItemStack.EMPTY : be.takeOutput();
            if (taken.isEmpty()) taken = be.removeLastIngredient();

            if (!taken.isEmpty()) {
                if (!player.getInventory().add(taken)) player.drop(taken, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
            } else if (player.isShiftKeyDown() && (be.getWaterMb() > 0 || be.getGasolineMb() > 0)) {
                be.discardLiquid();
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 0.7F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof IndustrialCauldronBlockEntity be) {
                for (ItemStack s : be.removeAllContents()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    // 💨 Partículas y sonido mientras cuece
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof IndustrialCauldronBlockEntity be) || !be.isBrewing()) return;

        double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;

        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.BUBBLE_POP, x, y - 0.05D, z, 0, 0.02D, 0);
        if (random.nextInt(6) == 0) level.addParticle(ParticleTypes.SMOKE, x, y + 0.05D, z, 0, 0.03D, 0);
        if (random.nextInt(25) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                    0.4F, 0.8F + random.nextFloat() * 0.4F, false);
        }
    }
}
