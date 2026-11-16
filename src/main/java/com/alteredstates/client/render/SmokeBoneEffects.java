package com.alteredstates.client.render;

import com.alteredstates.component.EmberState;
import com.alteredstates.item.ISmokableItem;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

/**
 * Efectos de los huesos "paper" y "tip" de CUALQUIER modelo de ítem fumable (cigarrillo, puro, pipa...).
 * No sabe nada de ningún ítem concreto: lee el estado de ISmokableItem (burnProgress, emberState, isTipVisible).
 *
 * CONVENCIÓN DE BLOCKBENCH (lo que hace que funcione con cualquier tamaño):
 *
 *  hueso "paper" (opcional)
 *     - su pivote va en el EXTREMO DEL FILTRO (donde empieza el papel) y sus cubos se extienden hacia +Y.
 *     - se encoge en Y hacia ese pivote según burnProgress.
 *     - la longitud se mide sobre TODOS sus cubos (de la base al extremo): vale un cubo único o un cono de varios
 *       segmentos apilados, de cualquier tamaño, sin constantes.
 *
 *  hueso "tip" (opcional)
 *     - debe ser HERMANO de "paper" (mismo hueso padre) para seguirlo; en una pipa va solo.
 *     - su pivote va en el CENTRO de sus cubos (el pulso escala alrededor del pivote).
 *     - con paper: se desplaza lo que el papel se acorta, siempre pegado a su extremo.
 *       Sin paper (pipa): no se mueve de sitio.
 *     - brilla con la brasa (capa de glow), palpita y se carboniza según emberState.
 *
 * Si un modelo no tiene alguno de los dos huesos, simplemente no se aplica ese efecto.
 */
public final class SmokeBoneEffects {
    public static final String PAPER = "paper";
    public static final String TIP = "tip";

    private SmokeBoneEffects() {}

    /**
     * Llamar para cada hueso justo ANTES de dibujarlo (también en la pasada del glow).
     * @return el color ARGB con el que hay que dibujar ese hueso.
     */
    public static int apply(GeoBone bone, ISmokableItem item, ItemStack stack, @Nullable Level level,
                            float partialTick, boolean isReRender, int colour, int glowTintRgb, float glowTintMix) {
        String name = bone.getName();
        if (PAPER.equals(name)) {
            float p = Mth.clamp(item.burnProgress(stack, level), 0.0f, 1.0f);
            bone.setScaleY(Math.max(p, 0.001f));
            return colour;
        }
        if (TIP.equals(name)) {
            return applyTip(bone, item, stack, level, partialTick, isReRender, colour, glowTintRgb, glowTintMix);
        }
        return colour;
    }

    private static int applyTip(GeoBone bone, ISmokableItem item, ItemStack stack, @Nullable Level level,
                                float partialTick, boolean isReRender, int colour, int glowTintRgb, float glowTintMix) {
        // Sin nada que arda (pipa vacía): se colapsa el hueso y no se aplica nada más.
        if (!item.isTipVisible(stack)) {
            bone.setScaleX(0.0f);
            bone.setScaleY(0.0f);
            bone.setScaleZ(0.0f);
            return colour;
        }

        float p = Mth.clamp(item.burnProgress(stack, level), 0.0f, 1.0f);

        // Pegado al papel: baja lo que el papel se acorta. Sin papel (pipa) no toca la posición.
        float paperLen = paperLengthPx(bone);
        if (paperLen > 0.0f) {
            bone.setPosY(-paperLen * (1.0f - p));
        }

        EmberState ember = item.emberState(stack);
        float now = level == null ? partialTick : (float) level.getGameTime() + partialTick;
        float glow = Mth.clamp(ember.level(now), 0.0f, 1.0f);
        glow = glow * glow * (3.0f - 2.0f * glow); // suaviza inicio y final del fundido

        float pulse = 1.0f + 0.05f * glow * (Mth.sin(now * 0.9f) + 0.6f * Mth.sin(now * 2.3f + 1.7f));
        bone.setScaleX(pulse);
        bone.setScaleY(pulse);
        bone.setScaleZ(pulse);

        float charLevel = Math.max(ember.charLevel(now), ember.litTick() < 0L && p < 1.0f ? 1.0f : 0.0f);
        return isReRender ? glowColour(colour, glow, glowTintRgb, glowTintMix) : charColour(colour, charLevel);
    }

    /** Largo en px del papel (extensión en Y de todos sus cubos), por hueso. El modelo no cambia: se calcula una vez. */
    private static final Map<GeoBone, Float> PAPER_LENGTH = new WeakHashMap<>();

    /** Longitud en px del hueso "paper" hermano de este "tip"; 0 si no hay papel. */
    private static float paperLengthPx(GeoBone tip) {
        GeoBone parent = tip.getParent();
        if (parent == null) return 0.0f;
        for (GeoBone sibling : parent.getChildBones()) {
            if (PAPER.equals(sibling.getName())) {
                return PAPER_LENGTH.computeIfAbsent(sibling, SmokeBoneEffects::measureLengthPx);
            }
        }
        return 0.0f;
    }

    private static float measureLengthPx(GeoBone paper) {
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (GeoCube cube : paper.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    float y = vertex.position().y();
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxY > minY ? (maxY - minY) * 16.0f : 0.0f; // las posiciones de vértice están en bloques
    }

    private static int glowColour(int argb, float glow, int tintRgb, float tintMix) {
        int alpha = Math.round((float) (argb >>> 24 & 0xFF) * glow);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        // La glowmask trae algún píxel cian: se convierte a tonos de brasa para que no se vea un fantasma azul.
        float hottest = Math.max(sourceRed, Math.max(sourceGreen, sourceBlue));
        float warmRed = Math.max((float) sourceRed, hottest * 0.85f);
        float greenLimit = warmRed * Mth.lerp(glow, 0.18f, 0.62f);
        int red = Math.round(warmRed);
        int green = Math.round(Math.min(Math.max((float) sourceGreen, hottest * 0.12f), greenLimit));
        int blue = Math.round(Math.min((float) sourceBlue, warmRed * 0.06f));
        // Tinte propio del ítem (joint verde): mezcla la brasa con ese color, a la intensidad que marca la máscara.
        if (tintMix > 0.0f) {
            float k = hottest / 255.0f;
            red = Math.round(Mth.lerp(tintMix, (float) red, (tintRgb >>> 16 & 0xFF) * k));
            green = Math.round(Mth.lerp(tintMix, (float) green, (tintRgb >>> 8 & 0xFF) * k));
            blue = Math.round(Mth.lerp(tintMix, (float) blue, (tintRgb & 0xFF) * k));
        }
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int charColour(int argb, float charLevel) {
        float amount = Mth.clamp(charLevel, 0.0f, 1.0f);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        float luminance = sourceRed * 0.2126f + sourceGreen * 0.7152f + sourceBlue * 0.0722f;
        float charcoal = luminance * 0.28f;
        int red = Math.round(Mth.lerp(amount, (float) sourceRed, charcoal));
        int green = Math.round(Mth.lerp(amount, (float) sourceGreen, charcoal));
        int blue = Math.round(Mth.lerp(amount, (float) sourceBlue, charcoal));
        return (argb & 0xFF000000) | red << 16 | green << 8 | blue;
    }
}
