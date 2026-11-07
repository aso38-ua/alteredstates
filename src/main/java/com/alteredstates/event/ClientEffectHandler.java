package com.alteredstates.event;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

/**
 * Gestiona los efectos visuales de pantalla para Psychodelia, Oniric Trance y Shaders.
 *
 * COMPATIBILIDAD CON MINECRAFT VANILLA Y SHADERS (Iris / Oculus / Optifine):
 * 1. En Vanilla (sin shaders externos): Se carga el shader de post-procesado nativo (viaje.json),
 *    que intercambia canales de color (R->G->B->R) y satura el mundo 3D.
 * 2. Con Shaders (Iris/Oculus con shaderpack activo): Como los shaders externos reemplazan
 *    el pipeline de post-processing vanilla, se activa un overlay dinámico psicodélico
 *    con blending aditivo y cambio continuo de colores directamente sobre la GUI,
 *    garantizando que el efecto visual se vea espectacular en cualquier configuración.
 */
@EventBusSubscriber(modid = AlteredStates.MOD_ID, value = Dist.CLIENT)
public class ClientEffectHandler {

    private static final ResourceLocation VIAJE_SHADER = ResourceLocation.fromNamespaceAndPath("alteredstates", "shaders/post/viaje.json");
    private static boolean isTripShaderActive = false;

    // Tick counter para animaciones suaves
    private static float animationTick = 0;

    // Fundido suave de los overlays de cocaína (0 = invisible, 1 = a tope)
    private static float rushFade = 0.0F;
    private static float crashFade = 0.0F;

    /**
     * Incrementar el contador de animación y gestionar la carga/descarga del shader post-process vanilla.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameRenderer == null) return;

        boolean hasPsychodelia = mc.player.hasEffect(ModEffects.PSYCHODELIA);
        boolean hasOniricTrance = mc.player.hasEffect(ModEffects.ONIRIC_TRANCE);
        boolean hasTripEffect = hasPsychodelia || hasOniricTrance;

        boolean hasRush = mc.player.hasEffect(ModEffects.COCAINE_RUSH);
        boolean hasCrash = mc.player.hasEffect(ModEffects.COCAINE_CRASH);
        rushFade = approach(rushFade, hasRush ? 1.0F : 0.0F, hasRush ? 0.012F : 0.02F);
        crashFade = approach(crashFade, hasCrash ? 1.0F : 0.0F, hasCrash ? 0.012F : 0.02F);

        if (hasTripEffect || rushFade > 0.001F || crashFade > 0.001F) {
            animationTick += 1.0F;
        } else {
            animationTick = 0;
        }

        if (!com.alteredstates.Config.ENABLE_SCREEN_SHADERS.get()) {
            if (isTripShaderActive) {
                mc.gameRenderer.shutdownEffect();
                isTripShaderActive = false;
            }
            return;
        }

        boolean shadersActive = isShaderpackActive();

        // En vanilla (sin shaders externos), cargamos el shader nativo viaje.json para Psychodelia
        if (hasPsychodelia && !shadersActive) {
            if (!isTripShaderActive) {
                try {
                    mc.gameRenderer.loadEffect(VIAJE_SHADER);
                    isTripShaderActive = true;
                } catch (Exception e) {
                    AlteredStates.LOGGER.error("[AlteredStates] Could not load viaje shader", e);
                }
            }
        } else {
            if (isTripShaderActive) {
                mc.gameRenderer.shutdownEffect();
                isTripShaderActive = false;
            }
        }
    }

    /**
     * Renderizar los overlays visuales directamente en el HUD.
     * Funciona tanto con shaders activos como fallback para vanilla.
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!com.alteredstates.Config.ENABLE_SCREEN_SHADERS.get()) return;

        boolean hasPsychodelia = mc.player.hasEffect(ModEffects.PSYCHODELIA);
        boolean hasOniricTrance = mc.player.hasEffect(ModEffects.ONIRIC_TRANCE);

        boolean showCocaine = rushFade > 0.001F || crashFade > 0.001F;
        if (!hasPsychodelia && !hasOniricTrance && !showCocaine) return;

        Matrix4f pose = event.getGuiGraphics().pose().last().pose();
        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();

        float partialTick = event.getPartialTick() != null ? event.getPartialTick().getGameTimeDeltaPartialTick(false) : 0.0F;
        float time = (animationTick + partialTick) * 0.05F;

        // Si hay Psychodelia y los shaders externos están activos O si el shader nativo no está activo
        if (hasPsychodelia && (isShaderpackActive() || !isTripShaderActive)) {
            renderPsychodeliaOverlay(pose, screenWidth, screenHeight, time);
        }

        if (hasOniricTrance) {
            renderOniricTranceOverlay(pose, screenWidth, screenHeight, time);
        }

        if (rushFade > 0.001F) {
            renderRushOverlay(pose, screenWidth, screenHeight, time, rushFade);
        }
        if (crashFade > 0.001F) {
            renderCrashOverlay(pose, screenWidth, screenHeight, time, crashFade);
        }
    }

    private static float approach(float value, float target, float step) {
        if (value < target) return Math.min(target, value + step);
        return Math.max(target, value - step);
    }

    /**
     * Subidón de cocaína: velo cálido tipo éxtasis. Pasteles (rosa, dorado, turquesa) que se
     * desplazan despacio por las esquinas, con un pulso lento y un brillo suave en los bordes.
     */
    private static void renderRushOverlay(Matrix4f pose, int w, int h, float time, float fade) {
        float t = time * 0.35F;
        float pulse = 0.85F + 0.15F * (float) Math.sin(time * 1.1F);
        float a = 0.13F * fade * pulse;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Cada esquina recorre despacio pastel rosa -> dorado -> turquesa
        float[] tl = pastel(t);
        float[] tr = pastel(t + 2.1F);
        float[] br = pastel(t + 4.2F);
        float[] bl = pastel(t + 1.0F);

        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(pose, 0, h, 0).setColor(bl[0], bl[1], bl[2], a);
        buf.addVertex(pose, w, h, 0).setColor(br[0], br[1], br[2], a);
        buf.addVertex(pose, w, 0, 0).setColor(tr[0], tr[1], tr[2], a);
        buf.addVertex(pose, 0, 0, 0).setColor(tl[0], tl[1], tl[2], a);
        BufferUploader.drawWithShader(buf.buildOrThrow());

        // Brillo suave en los bordes (viñeta luminosa)
        renderVignette(pose, w, h, 1.0F, 0.85F, 0.6F, 0.20F * fade * pulse, 0.22F);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Bajón: pantalla apagada y grisácea con viñeta oscura que respira despacio. */
    private static void renderCrashOverlay(Matrix4f pose, int w, int h, float time, float fade) {
        float breathe = 0.9F + 0.1F * (float) Math.sin(time * 0.5F);
        renderColorOverlay(pose, 0, 0, w, h, 0.42F, 0.45F, 0.52F, 0.26F * fade * breathe);
        renderVignette(pose, w, h, 0.05F, 0.06F, 0.10F, 0.55F * fade * breathe, 0.30F);
    }

    /** Color pastel suave a partir de una fase. */
    private static float[] pastel(float phase) {
        return new float[] {
                0.75F + 0.25F * (float) Math.sin(phase),
                0.65F + 0.25F * (float) Math.sin(phase + 2.094F),
                0.75F + 0.25F * (float) Math.sin(phase + 4.188F)
        };
    }

    /** Viñeta: 4 franjas en los bordes que se desvanecen hacia el centro. */
    private static void renderVignette(Matrix4f pose, int w, int h, float r, float g, float b, float edgeAlpha, float thickness) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        float tx = w * thickness;
        float ty = h * thickness;

        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // arriba
        buf.addVertex(pose, 0, ty, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, w, ty, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, w, 0, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, 0, 0, 0).setColor(r, g, b, edgeAlpha);
        // abajo
        buf.addVertex(pose, 0, h, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, w, h, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, w, h - ty, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, 0, h - ty, 0).setColor(r, g, b, 0.0F);
        // izquierda
        buf.addVertex(pose, 0, h, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, tx, h, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, tx, 0, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, 0, 0, 0).setColor(r, g, b, edgeAlpha);
        // derecha
        buf.addVertex(pose, w - tx, h, 0).setColor(r, g, b, 0.0F);
        buf.addVertex(pose, w, h, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, w, 0, 0).setColor(r, g, b, edgeAlpha);
        buf.addVertex(pose, w - tx, 0, 0).setColor(r, g, b, 0.0F);
        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.disableBlend();
    }

    /**
     * Efecto visual Psychodelia para Shaders / Fallback:
     * Capa 1: Mezcla aditiva de arcoíris rotativo dinámico en las 4 esquinas (simula rotación de canales).
     * Capa 2: Pulso rítmico cálido/magenta psicodélico que oscila continuamente.
     */
    private static void renderPsychodeliaOverlay(Matrix4f pose, int width, int height, float time) {
        // Capa 1: Mezcla aditiva que intensifica y cambia los colores de la escena
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float r1 = 0.5F + 0.5F * (float) Math.sin(time);
        float g1 = 0.5F + 0.5F * (float) Math.sin(time + 2.094F);
        float b1 = 0.5F + 0.5F * (float) Math.sin(time + 4.188F);

        float r2 = 0.5F + 0.5F * (float) Math.sin(time + 1.57F);
        float g2 = 0.5F + 0.5F * (float) Math.sin(time + 3.664F);
        float b2 = 0.5F + 0.5F * (float) Math.sin(time + 5.758F);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(pose, 0, height, 0).setColor(r1, g1, b1, 0.22F);
        buffer.addVertex(pose, width, height, 0).setColor(g1, b1, r1, 0.22F);
        buffer.addVertex(pose, width, 0, 0).setColor(r2, g2, b2, 0.22F);
        buffer.addVertex(pose, 0, 0, 0).setColor(g2, b2, r2, 0.22F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Capa 2: Tinte psicodélico pulsante en modo normal
        RenderSystem.defaultBlendFunc();
        float pulse = 0.08F + 0.04F * (float) Math.sin(time * 2.0F);
        float rPulse = 0.8F + 0.2F * (float) Math.sin(time * 1.7F);
        float gPulse = 0.2F + 0.2F * (float) Math.cos(time * 1.3F);
        float bPulse = 0.7F + 0.3F * (float) Math.sin(time * 0.9F);
        renderColorOverlay(pose, 0, 0, width, height, rPulse, gPulse, bPulse, pulse);

        RenderSystem.disableBlend();
    }

    /**
     * Efecto visual Oniric Trance: velo carmesí místico con pulsaciones lentas.
     */
    private static void renderOniricTranceOverlay(Matrix4f pose, int width, int height, float time) {
        float pulse = 0.16F + 0.08F * (float) Math.sin(time * 0.6F);
        renderColorOverlay(pose, 0, 0, width, height, 0.65F, 0.04F, 0.12F, pulse);

        float glow = 0.06F * (float) Math.max(0, Math.sin(time * 0.7F + 1.5F));
        if (glow > 0.005F) {
            renderColorOverlay(pose, 0, 0, width, height, 0.85F, 0.15F, 0.35F, glow);
        }
    }

    /**
     * Renderiza un rectángulo de color sólido usando la matriz de pose de la GUI.
     */
    private static void renderColorOverlay(Matrix4f pose, int x, int y, int width, int height, float r, float g, float b, float a) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(pose, x, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x + width, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x + width, y, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x, y, 0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }

    /**
     * Comprueba mediante reflexión si Iris, Oculus u Optifine tienen un shaderpack externo activo.
     */
    public static boolean isShaderpackActive() {
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApiClass.getMethod("getInstance").invoke(null);
            Object inUse = irisApiClass.getMethod("isShaderPackInUse").invoke(instance);
            if (inUse instanceof Boolean b && b) return true;
        } catch (Throwable ignored) {}

        try {
            Class<?> ofConfig = Class.forName("optifine.Config");
            Object inUse = ofConfig.getMethod("isShaders").invoke(null);
            if (inUse instanceof Boolean b && b) return true;
        } catch (Throwable ignored) {}

        return false;
    }
}