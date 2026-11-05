package com.alteredstates.client.render;

import com.alteredstates.item.CigaretteItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Draws the glowmask over the intact base texture, so fading never reveals the background. */
public class CigaretteGlowLayer extends GeoRenderLayer<CigaretteItem> {
    private static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "alteredstates", "textures/item/cigarette_glowmask.png");

    public CigaretteGlowLayer(GeoRenderer<CigaretteItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, CigaretteItem animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        RenderType glowRenderType = RenderType.entityTranslucent(GLOW_TEXTURE);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                bufferSource.getBuffer(glowRenderType), partialTick, LightTexture.FULL_BRIGHT,
                packedOverlay, getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt());
    }
}
