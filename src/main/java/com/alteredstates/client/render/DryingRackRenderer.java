package com.alteredstates.client.render;

import com.alteredstates.block.entity.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DryingRackBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack[] items = blockEntity.getItems();

        poseStack.pushPose();
        BlockRotationHelper.applyFacing(poseStack, blockEntity.getBlockState());

        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack.isEmpty()) continue;

            poseStack.pushPose();

            // Centrado en X, justo sobre la rejilla, alineado con los listones en Z
            double zPos = (2.5D + (i * 2.0D)) / 16.0D;
            poseStack.translate(0.5D, 0.76D, zPos);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            BakedModel model = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
            this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack,
                    bufferSource, combinedLight, combinedOverlay, model);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}