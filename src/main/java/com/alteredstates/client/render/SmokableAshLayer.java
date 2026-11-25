package com.alteredstates.client.render;

import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SmokableAshLayer extends GeoRenderLayer<SmokableItem> {
    private static final ResourceLocation ASH =
            ResourceLocation.fromNamespaceAndPath("alteredstates", "textures/item/ash.png");

    public SmokableAshLayer(GeoRenderer<SmokableItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, SmokableItem animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(this.getRenderer() instanceof SmokableRenderer r)) return;
        ItemStack stack = r.getCurrentItemStack();
        float a = SmokeBoneEffects.ashAlpha(animatable, stack, Minecraft.getInstance().level, partialTick);
        if (a <= 0.003f) return;

        RenderType ashType = RenderType.entityTranslucent(ASH);
        int argb = (Math.round(a * 255.0f) << 24) | 0x00FFFFFF;
        r.setAshPass(true);
        try {
            r.reRender(bakedModel, poseStack, bufferSource, (SmokableItem) animatable, ashType,
                    bufferSource.getBuffer(ashType), partialTick, packedLight, packedOverlay, argb);
        } finally {
            r.setAshPass(false);
        }
    }
}