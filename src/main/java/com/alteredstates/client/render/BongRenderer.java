package com.alteredstates.client.render;

import com.alteredstates.block.entity.BongBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BongRenderer implements BlockEntityRenderer<BongBlockEntity> {
    private final ItemRenderer itemRenderer;

    public BongRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BongBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack content = blockEntity.getBowlContent();
        if (content.isEmpty()) return;

        poseStack.pushPose();
        BlockRotationHelper.applyFacing(poseStack, blockEntity.getBlockState());

        // Posición de la cazoleta (el modelo no es simétrico, por eso hay que rotarla)
        poseStack.translate(0.91D, 0.46D, 0.5D);
        poseStack.scale(0.20F, 0.20F, 0.20F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-22.5F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90F));

        BakedModel model = this.itemRenderer.getModel(content, blockEntity.getLevel(), null, 0);
        this.itemRenderer.render(content, ItemDisplayContext.FIXED, false, poseStack,
                bufferSource, combinedLight, combinedOverlay, model);

        poseStack.popPose();
    }
}