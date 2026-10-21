package com.alteredstates.client;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

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

        if (!com.alteredstates.Config.ENABLE_SCREEN_SHADERS.get()) return;

        // Comprobamos si el jugador va fumado (Indica o Sativa)
        boolean isHigh = mc.player.hasEffect(ModEffects.INDICA_EFFECT) || mc.player.hasEffect(ModEffects.SATIVA_EFFECT);

        if (isHigh) {
            int screenWidth = event.getGuiGraphics().guiWidth();
            int screenHeight = event.getGuiGraphics().guiHeight();

            // Calculamos el frame actual: cambiamos de imagen cada 4 ticks (aprox 5 frames por segundo)
            int currentFrame = (mc.player.tickCount / 4) % 8;

            // Renderizamos la textura suavemente usando la matriz de pose actual de la GUI y color RGBA embebido en los vértices.
            // La opacidad es suave y configurable en la config (por defecto 0.35F para no saturar la vista).
            float opacity = com.alteredstates.Config.STONED_OVERLAY_OPACITY.get().floatValue();
            renderTexturedQuad(event.getGuiGraphics().pose().last().pose(), FRAMES[currentFrame], 0, 0, screenWidth, screenHeight, opacity);
        }
    }

    /**
     * Renderiza un quad texturizado con transparencia embebida directamente en los vértices.
     * Este método NO usa RenderSystem.setShaderColor(), por lo que es compatible con Iris shaders.
     * La transparencia se aplica directamente en el color de cada vértice (canal alfa RGBA).
     */
    private static void renderTexturedQuad(Matrix4f pose, ResourceLocation texture, int x, int y, int width, int height, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(pose, x, y + height, 0).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        buffer.addVertex(pose, x + width, y + height, 0).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        buffer.addVertex(pose, x + width, y, 0).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        buffer.addVertex(pose, x, y, 0).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }
}