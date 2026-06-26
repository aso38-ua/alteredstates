package com.alteredstates.event;

import com.alteredstates.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class ClientEffectHandler {

    private static boolean isTripShaderActive = false;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.gameRenderer == null) return;

        boolean hasPsychodelia = mc.player.hasEffect(ModEffects.PSYCHODELIA);

        if (hasPsychodelia && !isTripShaderActive) {
            ResourceLocation shaderPath = ResourceLocation.fromNamespaceAndPath("minecraft", "shaders/post/invert.json");
            try {
                mc.gameRenderer.loadEffect(shaderPath);
                isTripShaderActive = true;
            } catch (Exception ignored) {}

        } else if (!hasPsychodelia && isTripShaderActive) {
            mc.gameRenderer.shutdownEffect();
            isTripShaderActive = false;
        }
    }
}