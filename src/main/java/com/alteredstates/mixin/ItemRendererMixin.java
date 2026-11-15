package com.alteredstates.mixin;

import com.alteredstates.client.animation.FirstPersonRig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sustituye el render vanilla del ítem en la mano principal (primera persona) por el del propio
 * renderer del ítem, aplicando antes el origen canónico del rig.
 *
 * Qué se salta al cancelar: ItemRenderer#render, o sea la transformación "display" del JSON del
 * modelo y el recentrado -0.5. Así los display de firstperson_* dejan de afectar a la animación.
 * Lo que NO se salta: bobbing, equip y swing de ItemInHandRenderer, que ya están en el PoseStack.
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
        method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;"
               + "Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;"
               + "Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
        at = @At("HEAD"),
        cancellable = true)
    private void alteredstates$firstPersonRig(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
                                              boolean leftHand, PoseStack poseStack, MultiBufferSource buffer,
                                              Level level, int light, int overlay, int seed, CallbackInfo ci) {
        // Paso 1: solo mano derecha principal. Todo lo demás sigue por el camino vanilla.
        if (!FirstPersonRig.isFirstPerson(ctx) || !FirstPersonRig.isMainHandContext(ctx)) return;

        BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(stack).getCustomRenderer();
        if (!(renderer instanceof FirstPersonRig.Host)) return;

        // OJO: a diferencia de Immersive Eating NO salimos si el jugador está "usando" el ítem:
        // fumar ES usar el ítem y queremos que pase por el rig también entonces.
        poseStack.pushPose();
        FirstPersonRig.applyOrigin(poseStack, ctx);
        renderer.renderByItem(stack, ctx, poseStack, buffer, light, overlay);
        poseStack.popPose();
        ci.cancel();
    }
}
