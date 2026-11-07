package com.alteredstates.block;

import com.alteredstates.item.CuttingAgents;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Espejo de corte: polvo en una mano y aditivo en la otra (da igual cuál), click derecho.
 * Reglas en CuttingAgents (calidad y rendimiento según el aditivo).
 */
public class CuttingMirrorBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    public CuttingMirrorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack a = player.getItemInHand(hand);
        ItemStack b = player.getItemInHand(other);

        ItemStack powder, agent;
        if (a.is(ModItems.COCAINE_POWDER.get()) && CuttingAgents.qualityOf(b) >= 0) { powder = a; agent = b; }
        else if (b.is(ModItems.COCAINE_POWDER.get()) && CuttingAgents.qualityOf(a) >= 0) { powder = b; agent = a; }
        else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            int agentQ = CuttingAgents.qualityOf(agent);
            int inQ = powder.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2);
            int outQ = CuttingAgents.resultQuality(inQ, agentQ);
            int count = CuttingAgents.yieldFor(agentQ);

            ItemStack result = new ItemStack(ModItems.COCAINE_POWDER.get(), count);
            result.set(ModDataComponentTypes.QUALITY.get(), outQ);

            ItemStack particleStack = powder.copy();
            if (!player.hasInfiniteMaterials()) {
                powder.shrink(1);
                agent.shrink(1);
            }
            if (!player.getInventory().add(result)) player.drop(result, false);

            level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0F, 1.2F);
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D, 6, 0.2D, 0.02D, 0.2D, 0.02D);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
