package com.alteredstates.client.render;

import com.alteredstates.client.animation.ArmAnchor;
import com.alteredstates.client.animation.FirstPersonRig;
import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

/**
 * Renderer ÚNICO para todos los ítems fumables (cigarrillos, puros, pipas...). No hay que crear uno por ítem:
 * el modelo/textura/animación salen de item.visuals() (SmokableModel) y el brillo de SmokableGlowLayer.
 *
 * Qué hace aquí:
 *  - primera persona: captura los huesos rightArm/leftArm y dibuja el brazo real (FirstPersonRig + ArmAnchor);
 *  - todos los huesos: aplica los efectos de "paper" y "tip" (SmokeBoneEffects).
 */
public class SmokableRenderer extends GeoItemRenderer<SmokableItem> implements FirstPersonRig.Host {
    private final FirstPersonRig rig = new FirstPersonRig();

    public SmokableRenderer() {
        super(new SmokableModel());
        this.addRenderLayer(new SmokableGlowLayer(this));
    }

    /** Lo usa SmokableModel para elegir el geo de primera persona. */
    public boolean isFirstPersonRender() {
        return FirstPersonRig.isFirstPerson(this.renderPerspective);
    }

    /** El mixin de primera persona solo actúa sobre ítems con visuales propios (no sobre SmokeVisuals.NONE). */
    @Override
    public boolean usesFirstPersonRig(ItemStack stack) {
        return stack.getItem() instanceof SmokableItem item && item.visuals().isCustom();
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
    public void renderRecursively(PoseStack ps, SmokableItem item, GeoBone bone, RenderType rt,
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

        // Efectos de los huesos "paper" y "tip" (consumo, brasa, carbonizado): iguales para todo ítem fumable.
        int effectColour = SmokeBoneEffects.apply(bone, item, this.getCurrentItemStack(),
                Minecraft.getInstance().level, partialTick, isReRender, colour,
                item.visuals().glowTintRgb(), item.visuals().glowTintMix());
        super.renderRecursively(ps, item, bone, rt, buf, vc, isReRender, partialTick, light, overlay, effectColour);
    }
}
