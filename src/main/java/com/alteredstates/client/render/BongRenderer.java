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
    public void render(BongBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack weed = blockEntity.getBowlContent();

        // Si la cazoleta tiene hierba, la dibujamos
        if (!weed.isEmpty()) {
            poseStack.pushPose();

            // 📍 Movemos el ítem exactamente a las coordenadas del tubito metálico
            poseStack.translate(0.72D, 0.35D, 0.5D);

            // 🔍 Lo hacemos pequeñito para que quepa en la cazoleta
            poseStack.scale(0.25F, 0.25F, 0.25F);

            // 📐 Inclinamos el ítem para que siga el ángulo del tubito y lo tumbamos
            poseStack.mulPose(Axis.ZP.rotationDegrees(-22.5F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            // Llamamos a tu método renderItem
            renderItem(weed, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);

            poseStack.popPose();
        }
    }

    // El mismo método auxiliar que usaste en la bandeja
    private void renderItem(ItemStack stack, BongBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BakedModel bakedModel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, light, overlay, bakedModel);
    }
}