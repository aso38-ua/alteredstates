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

        // 🟢 Si el inventario del bong (la cazoleta) tiene algo de hierba...
        if (!weed.isEmpty()) {
            poseStack.pushPose();

            // 📍 Posición exacta en la nueva cazoleta de pizarra
            poseStack.translate(0.91D, 0.46D, 0.5D);

            // 🔍 Hacemos la textura de la hierba muy pequeñita (15% de su tamaño real)
            poseStack.scale(0.20F, 0.20F, 0.20F);

            // 📐 Inclinamos la hierba para que se asiente plana (-22.5 grados por el tubo)
            poseStack.mulPose(Axis.ZP.rotationDegrees(-22.5F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90F)); // Tumbamos el ítem 2D como en la bandeja

            // Dibujamos el ítem en la pantalla
            renderItem(weed, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);

            poseStack.popPose();
        }
    }

    // El mismo método auxiliar que usaste para la RollingTray
    private void renderItem(ItemStack stack, BongBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BakedModel bakedModel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, light, overlay, bakedModel);
    }
}