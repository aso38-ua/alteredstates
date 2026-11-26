package com.alteredstates.mixin;

import com.alteredstates.AlteredStates;
import com.alteredstates.client.animation.FirstPersonRig;
import com.alteredstates.item.SmokableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "punchy.config.PunchyConfig")
public class PunchyConfigMixin {
    private static Item alteredstates$lastItem; // TEMPORAL: solo para diagnóstico

    @Inject(method = "isModEnabled()Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void alteredstates$disablePunchyForRigItems(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack held = mc.player.getMainHandItem();
        boolean rig = !held.isEmpty() && held.getItem() instanceof SmokableItem;
        Item item = held.getItem();
        if (item != alteredstates$lastItem) {
            alteredstates$lastItem = item;
            Object renderer = held.isEmpty() ? null : IClientItemExtensions.of(held).getCustomRenderer();
            /*AlteredStates.LOGGER.info("[AS-Punchy] item={} usesRig={} renderer={} mainArm={}",
                    BuiltInRegistries.ITEM.getKey(item), rig,
                    renderer == null ? null : renderer.getClass().getName(), mc.player.getMainArm());*/
        }
        if (rig) {
            cir.setReturnValue(false);
        }
    }
}