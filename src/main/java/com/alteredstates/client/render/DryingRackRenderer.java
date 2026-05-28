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
    public void render(DryingRackBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack[] items = blockEntity.getItems();

        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack.isEmpty()) continue; // Saltamos huecos vacíos

            poseStack.pushPose();

            // 📍 NUEVAS MATEMÁTICAS GEOMÉTRICAS CORREGIDAS

            // X: Centrado en el medio del bloque (0.5D)
            double xPos = 0.5D;

            // Y: Justo encima de la rejilla de madera. Subimos a 0.76D para evitar Z-fighting.
            double yPos = 0.76D;

            // Z: El modelo tiene 7 listones de madera (Slat1 a Slat7).
            // Estos listones están centrados geométricamente en estas coordenadas D del bloque:
            // Slat1=0.156, Slat2=0.281, Slat3=0.406, Slat4=0.531, Slat5=0.656, Slat6=0.781.
            // Usamos una fórmula precisa para alinear los 6 ítems con los 6 primeros listones.
            double zPos = (2.5D + (i * 2.0D)) / 16.0D;

            poseStack.translate(xPos, yPos, zPos);

            // Escala para que los 6 cogollos quepan bien alineados (un pelín más pequeños: 0.3F)
            poseStack.scale(0.3F, 0.3F, 0.3F);

            // Rotamos el ítem plano sobre la rejilla (tumbado)
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            // Renderizamos este ítem en su posición calculada
            BakedModel bakedModel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
            this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, combinedLight, combinedOverlay, bakedModel);

            poseStack.popPose();
        }
    }
}