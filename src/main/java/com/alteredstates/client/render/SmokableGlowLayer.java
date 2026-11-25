package com.alteredstates.client.render;

import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SmokableGlowLayer extends GeoRenderLayer<SmokableItem> {
    public SmokableGlowLayer(GeoRenderer<SmokableItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, SmokableItem animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(this.getRenderer() instanceof SmokableRenderer r)) {
            return;
        }
        ItemStack stack = r.getCurrentItemStack();
        Level level = Minecraft.getInstance().level;
        float now = level == null ? partialTick : (float) level.getGameTime() + partialTick;
        if (animatable.emberState(stack).level(now) <= 0.003f) {
            return; // brasa apagada: el pase glow tendría alpha 0 igualmente
        }
        ResourceLocation glow = animatable.visuals().glowTexture();
        // GeckoLib solo avanza el frame de la textura principal; el de la capa hay que pedirlo a mano
        AnimatableTexture.setAndUpdate(glow);
        RenderType glowRenderType = RenderType.entityTranslucent(glow);
        r.setGlowPass(true);
        try {
            r.reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                    bufferSource.getBuffer(glowRenderType), partialTick, 0xF000F0, packedOverlay,
                    r.getRenderColor(animatable, partialTick, packedLight).argbInt());
        } finally {
            r.setGlowPass(false);
        }
    }
}
