package com.alteredstates.mixin;

import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Renderiza el fumable equipado en cabeza como un objeto de boca en la vista de primera persona. */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    // Ajuste del cigarrillo en pantalla, en coordenadas locales de cámara.
    private static final float HEAD_VIEW_X = 0.0f;
    private static final float HEAD_VIEW_Y = -0.1f;
    private static final float HEAD_VIEW_Z = 0.06f;

    // Retardo vertical al saltar/caer: factor por velocidad vertical, desplazamiento máximo
    // y rapidez de suavizado (0..1 por frame).
    private static final float JUMP_LAG_FACTOR = 0.12f;
    private static final float JUMP_LAG_MAX = 0.05f;
    private static final float JUMP_LAG_SMOOTHING = 0.25f;

    // Valor suavizado entre frames (estado solo de cliente).
    private static float alteredstates$jumpLag = 0.0f;

    @Inject(
            method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"
                    + "Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At("TAIL"))
    private void alteredstates$renderEquippedSmokable(float partialTick, PoseStack poseStack,
                                                      MultiBufferSource.BufferSource buffer, LocalPlayer player,
                                                      int packedLight, CallbackInfo ci) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(head.getItem() instanceof SmokableItem item) || !item.visuals().isCustom()) {
            alteredstates$jumpLag = 0.0f;
            return;
        }

        // Objetivo: 0 en el suelo; en el aire, opuesto a la velocidad vertical.
        float target = 0.0f;
        if (!player.onGround()) {
            target = Mth.clamp((float) -player.getDeltaMovement().y * JUMP_LAG_FACTOR,
                    -JUMP_LAG_MAX, JUMP_LAG_MAX);
        }
        alteredstates$jumpLag = Mth.lerp(JUMP_LAG_SMOOTHING, alteredstates$jumpLag, target);

        poseStack.pushPose();
        // Deshace el sway del ratón y el balanceo al caminar para que el puro quede fijo en la boca.
        alteredstates$cancelViewMotion(poseStack, player, partialTick);
        poseStack.translate(HEAD_VIEW_X + item.headOffsetX(),
                HEAD_VIEW_Y + item.headOffsetY() + alteredstates$jumpLag,
                HEAD_VIEW_Z + item.headOffsetZ());
        poseStack.scale(0.3f, 0.3f, 0.3f);
        ((ItemInHandRenderer) (Object) this).renderItem(player, head, ItemDisplayContext.HEAD, false,
                poseStack, buffer, packedLight);
        poseStack.popPose();
        buffer.endBatch();   // vacía las pasadas glow/ash antes de salir del render de manos
    }

    /**
     * Aplica la inversa de lo que vanilla le ha hecho al PoseStack antes de llegar aquí:
     * primero el sway del ratón (renderHandsWithItems) y antes de eso bobView (GameRenderer).
     * Se deshacen en orden inverso: sway, luego bobView.
     */
    private static void alteredstates$cancelViewMotion(PoseStack ps, LocalPlayer player, float partialTick) {
        // Sway: vanilla aplicó Rx(swayX) y luego Ry(swayY); la inversa es Ry(-swayY) y luego Rx(-swayX).
        float xBob = Mth.lerp(partialTick, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partialTick, player.yBobO, player.yBob);
        float swayX = (player.getViewXRot(partialTick) - xBob) * 0.1f;
        float swayY = (player.getViewYRot(partialTick) - yBob) * 0.1f;
        ps.mulPose(Axis.YP.rotationDegrees(-swayY));
        ps.mulPose(Axis.XP.rotationDegrees(-swayX));

        // bobView: vanilla aplicó translate(tx, ty, 0), Rz(rz), Rx(rx); la inversa es Rx(-rx), Rz(-rz), translate(-tx, -ty, 0).
        if (Minecraft.getInstance().options.bobView().get()) {
            float walkDelta = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + walkDelta * partialTick);
            float f2 = Mth.lerp(partialTick, player.oBob, player.bob);
            float tx = Mth.sin(f1 * (float) Math.PI) * f2 * 0.5f;
            float ty = -Math.abs(Mth.cos(f1 * (float) Math.PI) * f2);
            float rz = Mth.sin(f1 * (float) Math.PI) * f2 * 3.0f;
            float rx = Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2f) * f2) * 5.0f;
            ps.mulPose(Axis.XP.rotationDegrees(-rx));
            ps.mulPose(Axis.ZP.rotationDegrees(-rz));
            ps.translate(-tx, -ty, 0.0f);
        }
    }
}