package com.alteredstates.mixin;

import com.alteredstates.item.SmokableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
    //private static final float HEAD_VIEW_X = 0.0f;
    //private static final float HEAD_VIEW_Y = -0.42f;
    //private static final float HEAD_VIEW_Z = -0.65f;
    private static final float HEAD_VIEW_X = 0.0f;
    private static final float HEAD_VIEW_Y = -0.1f;
    private static final float HEAD_VIEW_Z = 0.06f;

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
            return;
        }

        poseStack.pushPose();
        poseStack.translate(HEAD_VIEW_X + item.headOffsetX(), HEAD_VIEW_Y + item.headOffsetY(),
                HEAD_VIEW_Z + item.headOffsetZ());
        poseStack.scale(0.3f, 0.3f, 0.3f);
        ((ItemInHandRenderer) (Object) this).renderItem(player, head, ItemDisplayContext.HEAD, false,
                poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
