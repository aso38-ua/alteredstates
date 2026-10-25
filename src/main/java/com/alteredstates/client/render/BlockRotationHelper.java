package com.alteredstates.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockRotationHelper {
    private BlockRotationHelper() {}

    /** Rota el PoseStack alrededor del centro del bloque según su FACING. */
    public static void applyFacing(PoseStack poseStack, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }
}