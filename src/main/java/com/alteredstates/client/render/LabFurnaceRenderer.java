package com.alteredstates.client.render;

import com.alteredstates.block.entity.LabFurnaceBlockEntity;
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

/** Pasta a la izquierda y cocaína a la derecha, sobre la bandeja superior (sin GUI). */
public class LabFurnaceRenderer implements BlockEntityRenderer<LabFurnaceBlockEntity> {
    private static final float TRAY_Y = 13F / 16F; // base de la bandeja
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public LabFurnaceRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(LabFurnaceBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        if (be.getInput().isEmpty() && be.getOutput().isEmpty()) return;

        poseStack.pushPose();
        BlockRotationHelper.applyFacing(poseStack, be.getBlockState());
        draw(be, be.getInput(), 0.30D, poseStack, buffer, light, overlay, 0);
        draw(be, be.getOutput(), 0.70D, poseStack, buffer, light, overlay, 100);
        poseStack.popPose();
    }

    private void draw(LabFurnaceBlockEntity be, ItemStack stack, double x, PoseStack poseStack,
                      MultiBufferSource buffer, int light, int overlay, int seedOffset) {
        if (stack.isEmpty()) return;
        int shown = Math.min(4, stack.getCount());
        BakedModel model = this.itemRenderer.getModel(stack, be.getLevel(), null, 0);
        for (int i = 0; i < shown; i++) {
            this.random.setSeed(be.getBlockPos().asLong() + seedOffset + i * 17L);
            poseStack.pushPose();
            poseStack.translate(x + (random.nextFloat() - 0.5F) * 0.08F,
                    TRAY_Y + 0.02F + i * 0.025F,
                    0.34D + (random.nextFloat() - 0.5F) * 0.08F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360F));
            poseStack.scale(0.3F, 0.3F, 0.3F);
            this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, light, overlay, model);
            poseStack.popPose();
        }
    }
}
