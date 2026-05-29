package com.alteredstates.client.render;

import com.alteredstates.block.entity.RollingTrayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RollingTrayRenderer implements BlockEntityRenderer<RollingTrayBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RollingTrayRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RollingTrayBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack paper = blockEntity.getPaper();
        ItemStack weed = blockEntity.getWeed();
        ItemStack additive = blockEntity.getAdditive();

        if (!paper.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.13D, 0.5D); // A 2 píxeles de altura (sobre la madera)
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F)); // Lo tumba horizontal
            renderItem(paper, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
            poseStack.popPose();
        }

        if (!weed.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.15D, 0.5D); // Ligeramente encima del papel
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45F)); // Girado para dar caos
            renderItem(weed, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
            poseStack.popPose();
        }

        if (!additive.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.17D, 0.5D); // Encima de la hierba
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-30F));
            renderItem(additive, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
            poseStack.popPose();
        }
    }

    private void renderItem(ItemStack stack, RollingTrayBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BakedModel bakedModel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, light, overlay, bakedModel);
    }
}