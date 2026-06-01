package com.alteredstates.client;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = AlteredStates.MOD_ID, value = Dist.CLIENT)
public class StonedOverlay {

    // Pre-cargamos las 8 texturas para no saturar la memoria
    private static final ResourceLocation[] FRAMES = new ResourceLocation[8];
    static {
        for (int i = 0; i < 8; i++) {
            FRAMES[i] = ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "textures/gui/overlay/stoned_" + i + ".png");
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Comprobamos si el jugador va fumado (Indica o Sativa)
        boolean isHigh = mc.player.hasEffect(ModEffects.INDICA_EFFECT) || mc.player.hasEffect(ModEffects.SATIVA_EFFECT);

        if (isHigh) {
            int screenWidth = event.getGuiGraphics().guiWidth();
            int screenHeight = event.getGuiGraphics().guiHeight();

            // Calculamos el frame actual: cambiamos de imagen cada 4 ticks (aprox 5 frames por segundo)
            int currentFrame = (mc.player.tickCount / 4) % 8;

            // 🟢 MAGIA DE RENDERIZADO: Activar transparencia
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Color blanco puro, pero con un Canal Alfa (Transparencia) del 0.25F (25% visible)
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.20F);

            // Dibujar la textura ocupando toda la pantalla
            event.getGuiGraphics().blit(FRAMES[currentFrame], 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

            // 🔴 IMPORTANTE: Restaurar el color normal para no romper la interfaz del juego
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }
}