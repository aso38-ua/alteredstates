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

        if (!com.alteredstates.Config.ENABLE_SCREEN_SHADERS.get()) {
            if (isTripShaderActive) {
                mc.gameRenderer.shutdownEffect();
                isTripShaderActive = false;
            }
            return;
        }

        boolean hasTripEffect = mc.player.hasEffect(ModEffects.PSYCHODELIA) || mc.player.hasEffect(ModEffects.ONIRIC_TRANCE);

        if (hasTripEffect && !isTripShaderActive) {
            ResourceLocation shaderPath = ResourceLocation.fromNamespaceAndPath("alteredstates", "shaders/post/viaje.json");
            try {
                mc.gameRenderer.loadEffect(shaderPath);
                isTripShaderActive = true;
            } catch (Exception ignored) {}

        } else if (!hasTripEffect && isTripShaderActive) {
            mc.gameRenderer.shutdownEffect();
            isTripShaderActive = false;
        }
    }
}