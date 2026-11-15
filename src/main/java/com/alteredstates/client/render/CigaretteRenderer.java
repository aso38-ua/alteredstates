package com.alteredstates.client.render;

import com.alteredstates.client.animation.ArmAnchor;
import com.alteredstates.client.animation.FirstPersonRig;
import com.alteredstates.component.EmberState;
import com.alteredstates.item.CigaretteItem;
import com.alteredstates.registry.ModDataComponentTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtil;

/**
 * PASO 1 de la migración: el brazo y el origen de primera persona ya no viven aquí, sino en
 * client.anim (FirstPersonRig + ArmAnchor). Lo que queda es específico del cigarrillo:
 * consumo del paper, ascua (glow) y carbonizado. Eso se extraerá en el paso 3.
 */
public class CigaretteRenderer extends GeoItemRenderer<CigaretteItem> implements FirstPersonRig.Host {
    //private static final float PAPER_LEN_PX = 30.0f;

    private final FirstPersonRig rig = new FirstPersonRig();

    public CigaretteRenderer() {
        super(new CigaretteModel());
        this.addRenderLayer(new CigaretteGlowLayer((GeoRenderer<CigaretteItem>) this));
    }

    /** Lo usa CigaretteModel para elegir el geo de primera persona. */
    public boolean isFirstPersonRender() {
        return FirstPersonRig.isFirstPerson(this.renderPerspective);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        rig.begin();
        super.renderByItem(stack, context, poseStack, bufferSource, packedLight, packedOverlay);
        // Los brazos se dibujan DESPUÉS del ítem con matrices absolutas capturadas en renderRecursively.
        rig.flush(bufferSource, packedLight);
    }

    @Override
    public void renderRecursively(PoseStack ps, CigaretteItem item, GeoBone bone, RenderType rt,
                                  MultiBufferSource buf, VertexConsumer vc, boolean isReRender,
                                  float partialTick, int light, int overlay, int colour) {
        // Hueso de brazo en primera persona: no se dibuja su cubo; se captura para dibujar el brazo real.
        if (isFirstPersonRender() && ArmAnchor.isArmBone(bone)) {
            ps.pushPose();
            RenderUtil.prepMatrixForBone(ps, bone);
            if (!isReRender) {
                rig.captureIfArm(ps, bone);
                this.applyRenderLayersForBone(ps, item, bone, rt, buf, vc, partialTick, light, overlay);
            }
            this.renderChildBones(ps, item, bone, rt, buf, vc, isReRender, partialTick, light, overlay, colour);
            ps.popPose();
            return;
        }

        // ---- A partir de aquí, sin cambios respecto a tu versión: consumo, ascua y carbonizado ----
        ItemStack stack = this.getCurrentItemStack();
        ClientLevel level = Minecraft.getInstance().level;
        float p = Mth.clamp(CigaretteItem.getBurnProgress(stack, (Level) level), 0.0f, 1.0f);
        switch (bone.getName()) {
            case "paper" -> {
                bone.setScaleY(Math.max(p, 0.001f));
                /*if (!isReRender && level != null && level.getGameTime() % 20 == 0) {
                    System.out.println("[paper] p=" + p + " scaleY=" + bone.getScaleY());
                }*/
            }
            case "tip" -> {
                bone.setPosY(-paperLengthPx(bone) * (1.0f - p));
                EmberState ember = stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF);
                float now = nowTicks(partialTick);
                float glow = Mth.clamp(ember.level(now), 0.0f, 1.0f);
                glow = glow * glow * (3.0f - 2.0f * glow);
                float pulse = 1.0f + 0.05f * glow * (Mth.sin(now * 0.9f) + 0.6f * Mth.sin(now * 2.3f + 1.7f));
                bone.setScaleX(pulse);
                bone.setScaleY(pulse);
                bone.setScaleZ(pulse);
                float charLevel = Math.max(ember.charLevel(now), ember.litTick() < 0L && p < 1.0f ? 1.0f : 0.0f);
                colour = isReRender ? glowColour(colour, glow) : charColour(colour, charLevel);
            }
            default -> { }
        }
        super.renderRecursively(ps, item, bone, rt, buf, vc, isReRender, partialTick, light, overlay, colour);
    }

    private static float nowTicks(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? partialTick : (float) level.getGameTime() + partialTick;
    }

    private static int glowColour(int argb, float glow) {
        int alpha = Math.round((float) (argb >>> 24 & 0xFF) * glow);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        float hottest = Math.max(sourceRed, Math.max(sourceGreen, sourceBlue));
        float warmRed = Math.max((float) sourceRed, hottest * 0.85f);
        float greenLimit = warmRed * Mth.lerp(glow, 0.18f, 0.62f);
        int red = Math.round(warmRed);
        int green = Math.round(Math.min(Math.max((float) sourceGreen, hottest * 0.12f), greenLimit));
        int blue = Math.round(Math.min((float) sourceBlue, warmRed * 0.06f));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int charColour(int argb, float charLevel) {
        float amount = Mth.clamp(charLevel, 0.0f, 1.0f);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        float luminance = sourceRed * 0.2126f + sourceGreen * 0.7152f + sourceBlue * 0.0722f;
        float charcoal = luminance * 0.28f;
        int red = Math.round(Mth.lerp(amount, (float) sourceRed, charcoal));
        int green = Math.round(Mth.lerp(amount, (float) sourceGreen, charcoal));
        int blue = Math.round(Mth.lerp(amount, (float) sourceBlue, charcoal));
        return (argb & 0xFF000000) | red << 16 | green << 8 | blue;
    }

    private static float paperLengthPx(GeoBone tip) {
        GeoBone parent = tip.getParent();
        if (parent != null) {
            for (GeoBone sibling : parent.getChildBones()) {
                if ("paper".equals(sibling.getName()) && !sibling.getCubes().isEmpty()) {
                    return (float) sibling.getCubes().get(0).size().y;
                }
            }
        }
        return 30.0f;
    }
}
