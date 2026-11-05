package com.alteredstates.client.render;

import com.alteredstates.component.EmberState;
import com.alteredstates.item.CigaretteItem;
import com.alteredstates.registry.ModDataComponentTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CigaretteRenderer extends GeoItemRenderer<CigaretteItem> {
    private static final float PAPER_LEN_PX = 30f; // size Y del cubo de paper en tu modelo

    public CigaretteRenderer() {
        super(new CigaretteModel());
        addRenderLayer(new CigaretteGlowLayer(this));
    }

    @Override
    public void renderRecursively(PoseStack ps, CigaretteItem item, GeoBone bone, RenderType rt,
                                  MultiBufferSource buf, VertexConsumer vc, boolean isReRender, float partialTick,
                                  int light, int overlay, int colour) {

        ItemStack stack = getCurrentItemStack();
        Level level = Minecraft.getInstance().level;
        float p = Mth.clamp(CigaretteItem.getBurnProgress(stack, level), 0f, 1f);

        switch (bone.getName()) {
            case "paper" -> bone.setScaleY(Math.max(p, 0.001f));
            case "tip" -> {
                bone.setPosY(-PAPER_LEN_PX * (1f - p));
                EmberState ember = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
                float now = nowTicks(partialTick);
                float glow = Mth.clamp(ember.level(now), 0f, 1f);
                glow = glow * glow * (3f - 2f * glow); // suaviza inicio y final del fundido

                float pulse = 1f + 0.05f * glow
                        * (Mth.sin(now * 0.9f) + 0.6f * Mth.sin(now * 2.3f + 1.7f));
                bone.setScaleX(pulse);
                bone.setScaleY(pulse);
                bone.setScaleZ(pulse);

                float charLevel = Math.max(ember.charLevel(now),
                        ember.litTick() < 0 && p < 1f ? 1f : 0f);
                colour = isReRender ? glowColour(colour, glow) : charColour(colour, charLevel);
            }
        }
        super.renderRecursively(ps, item, bone, rt, buf, vc, isReRender,
                partialTick, light, overlay, colour);
    }

    private static float nowTicks(float partialTick) {
        Level level = Minecraft.getInstance().level;
        return level == null ? partialTick : level.getGameTime() + partialTick;
    }

    private static int glowColour(int argb, float glow) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * glow);
        int sourceRed = (argb >>> 16) & 0xFF;
        int sourceGreen = (argb >>> 8) & 0xFF;
        int sourceBlue = argb & 0xFF;

        // The glowmask contains a few cyan pixels. Convert those to warm ember tones
        // instead of letting them show through as a blue ghost while the glow fades.
        float hottest = Math.max(sourceRed, Math.max(sourceGreen, sourceBlue));
        float warmRed = Math.max(sourceRed, hottest * 0.85f);
        float greenLimit = warmRed * Mth.lerp(glow, 0.18f, 0.62f);
        int red = Math.round(warmRed);
        int green = Math.round(Math.min(Math.max(sourceGreen, hottest * 0.12f), greenLimit));
        int blue = Math.round(Math.min(sourceBlue, warmRed * 0.06f));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int charColour(int argb, float charLevel) {
        float amount = Mth.clamp(charLevel, 0f, 1f);
        int sourceRed = (argb >>> 16) & 0xFF;
        int sourceGreen = (argb >>> 8) & 0xFF;
        int sourceBlue = argb & 0xFF;
        float luminance = sourceRed * 0.2126f + sourceGreen * 0.7152f + sourceBlue * 0.0722f;
        float charcoal = luminance * 0.28f;
        int red = Math.round(Mth.lerp(amount, sourceRed, charcoal));
        int green = Math.round(Mth.lerp(amount, sourceGreen, charcoal));
        int blue = Math.round(Mth.lerp(amount, sourceBlue, charcoal));
        int alpha = argb & 0xFF000000; // conservar opaca la textura base
        return alpha | (red << 16) | (green << 8) | blue;
    }
}
