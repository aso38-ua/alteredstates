package com.alteredstates.client.render;

import com.alteredstates.block.entity.CuringJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CuringJarRenderer implements BlockEntityRenderer<CuringJarBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public CuringJarRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CuringJarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack stack = blockEntity.getStoredItem();

        if (stack.isEmpty() || stack.getCount() <= 0) return;

        int count = stack.getCount();
        // Aumentamos a 5 capas visuales para un stack completo de 64
        int layers = Math.min(5, (count + 12) / 13);

        poseStack.pushPose();

        // Centramos y bajamos un poco para el nuevo escalado
        poseStack.translate(0.5D, 0.08D, 0.5D);

        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();

            // 📍 EMPAQUETADO DENSO: Offset vertical súper reducido (0.05D por capa)
            double yOffset = i * 0.05D;
            poseStack.translate(0, yOffset, 0);

            // Ajuste de escala ligero para que queden holgados dentro pero sin mucho hueco
            poseStack.scale(0.4F, 0.4F, 0.4F);

            // Los tumbamos en horizontal
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            // 🔄 ROTACIÓN ORGÁNICA: Bucle para meter una rotación estética "sucia"
            // Usamos una semilla basada en la posición y la capa para que sea aleatoria pero fija visualmente
            long seed = (long)blockEntity.getBlockPos().hashCode() + (i * 31);
            this.random.setSeed(seed);

            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360F));

            // Renderizamos este ítem en su posición calculada
            BakedModel bakedModel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
            this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, combinedLight, combinedOverlay, bakedModel);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}