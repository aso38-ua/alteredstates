package com.alteredstates.client.render;

import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Brillo de la brasa. La textura (máscara) sale de los visuales del ítem. */
public class SmokableGlowLayer extends GeoRenderLayer<SmokableItem> {

    public SmokableGlowLayer(GeoRenderer<SmokableItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, SmokableItem animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        RenderType glowRenderType = RenderType.entityTranslucent(animatable.visuals().glowTexture());
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                bufferSource.getBuffer(glowRenderType), partialTick, 0xF000F0, packedOverlay,
                this.getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt());
    }
}
