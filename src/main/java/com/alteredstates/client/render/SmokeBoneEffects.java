package com.alteredstates.client.render;

import com.alteredstates.component.EmberState;
import com.alteredstates.item.ISmokableItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import com.alteredstates.component.SmokableData;
import com.alteredstates.registry.ModDataComponentTypes;

/**
 * Efectos por nombre de hueso compartidos por todos los fumables.
 *
 * <p>Huesos reconocidos (da igual dónde estén anidados; Blockbench añade sufijos como "tip2"):
 * <ul>
 *   <li><b>paper</b> (un solo hueso con cubos, nombre {@code paper}, {@code paper2}...): se encoge en Y con su
 *       base fija y el tip pegado a su final.</li>
 *   <li><b>paper_1 ... paper_N</b> (2 o más huesos con cubos): capas. Se consume desde la última (la más
 *       cercana al tip): esa capa se encoge, las ya consumidas se ocultan, el tip se pega al final de la capa
 *       viva y toma su anchura.</li>
 *   <li><b>tip</b> ({@code tip}, {@code tip2}...): brasa.</li>
 * </ul>
 * El modo (una capa / varias) se decide por cuántos huesos de papel con cubos hay en el modelo, no por el nombre.
 * El pivote de cada capa puede estar donde sea: se compensa para que su base no se mueva.
 */
public final class SmokeBoneEffects {
    /** Por debajo de esta fracción de altura una capa (salvo la primera) se oculta en vez de escalarse. */
    private static final float HIDE_BELOW = 0.002f;
    private static final float MIN_SCALE = 0.001f;

    // OJO: GeoBone.equals/hashCode comparan por valores (nombre, nombre del padre, nº de cubos e hijos), así que
    // dos modelos distintos con la misma forma (p. ej. todos los *_fp) serían "el mismo" hueso en un HashMap.
    // Por eso las cachés van por IDENTIDAD.
    private static final int CACHE_LIMIT = 128;
    private static final Map<GeoBone, PaperStack> STACKS = new IdentityHashMap<GeoBone, PaperStack>();
    private static final Map<GeoBone, Float> TIP_WIDTH = new IdentityHashMap<GeoBone, Float>();
    private static final PaperStack NO_PAPER = new PaperStack(new String[0], new float[0], new float[0], new float[0]);

    private SmokeBoneEffects() {
    }

    public static boolean isTip(String name) {
        return isTipName(name);
    }

    public static float ashAlpha(ISmokableItem item, ItemStack stack, @Nullable Level level, float partialTick) {
        SmokableData data = (SmokableData) stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        if (data == null) return 0.0f;
        float now = level == null ? (float) data.lastLitTick() : (float) level.getGameTime() + partialTick;
        float ash = data.ashLevel(now);
        if (ash <= 0.0f) return 0.0f;
        float glow = Mth.clamp(item.emberState(stack).level(now), 0.0f, 1.0f);
        return ash * (1.0f - glow);
    }

    public static int apply(GeoBone bone, ISmokableItem item, ItemStack stack, @Nullable Level level, float partialTick, boolean isReRender, int colour, int glowTintRgb, float glowTintMix) {
        String name = bone.getName();
        if (isPaperName(name)) {
            applyPaper(bone, item, stack, level);
            return colour;
        }
        if (isTipName(name)) {
            return applyTip(bone, item, stack, level, partialTick, isReRender, colour, glowTintRgb, glowTintMix);
        }
        return colour;
    }

    // ------------------------------------------------------------------ nombres

    /** paper, paper2, paper_1, paper_12 */
    private static boolean isPaperName(String name) {
        if (!name.startsWith("paper")) {
            return false;
        }
        int i = 5;
        if (i < name.length() && name.charAt(i) == '_') {
            i++;
            if (i >= name.length()) {
                return false;
            }
        }
        for (; i < name.length(); ++i) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** tip, tip2 */
    private static boolean isTipName(String name) {
        if (!name.startsWith("tip")) {
            return false;
        }
        for (int i = 3; i < name.length(); ++i) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ papel

    private static void applyPaper(GeoBone bone, ISmokableItem item, ItemStack stack, @Nullable Level level) {
        if (bone.getCubes().isEmpty()) {
            return; // grupo contenedor, no es una capa
        }
        PaperStack paper = stackFor(bone);
        int i = paper.indexOf(bone.getName());
        if (i < 0) {
            return;
        }
        float p = Mth.clamp((float)item.burnProgress(stack, level), (float)0.0f, (float)1.0f);
        float s = paper.scaleOf(i, paper.liveTop(p));
        if (paper.layers() > 1 && i > 0 && s <= HIDE_BELOW) {
            bone.setHidden(true);
            return;
        }
        bone.setHidden(false);
        s = Math.max(s, MIN_SCALE);
        bone.setScaleY(s);
        // La escala se aplica alrededor del pivote del hueso. Se desplaza para que equivalga a escalar
        // alrededor de la BASE de la capa, esté donde esté el pivote.
        bone.setPosY((paper.base[i] - bone.getPivotY()) * (1.0f - s));
    }

    private static PaperStack stackFor(GeoBone any) {
        GeoBone root = any;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        PaperStack cached = STACKS.get(root);
        if (cached == null) {
            if (STACKS.size() >= CACHE_LIMIT) {
                STACKS.clear(); // recarga de recursos: los huesos viejos ya no sirven
            }
            cached = buildStack(root);
            STACKS.put(root, cached);
        }
        return cached;
    }

    private static void collectPaper(GeoBone node, List<GeoBone> out) {
        if (isPaperName(node.getName()) && !node.getCubes().isEmpty()) {
            out.add(node);
        }
        for (GeoBone child : node.getChildBones()) {
            collectPaper(child, out);
        }
    }

    private static PaperStack buildStack(GeoBone root) {
        List<GeoBone> layers = new ArrayList<GeoBone>();
        collectPaper(root, layers);
        if (layers.isEmpty()) {
            return NO_PAPER;
        }
        int n = layers.size();
        float[][] b = new float[n][];
        for (int i = 0; i < n; ++i) {
            b[i] = bounds(layers.get(i));
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; ++i) {
            order[i] = i;
        }
        Arrays.sort(order, (x, y) -> Float.compare(b[x][2], b[y][2])); // de la base hacia el tip
        String[] names = new String[n];
        float[] base = new float[n];
        float[] top = new float[n];
        float[] width = new float[n];
        for (int k = 0; k < n; ++k) {
            int i = order[k];
            names[k] = layers.get(i).getName();
            base[k] = b[i][2];
            top[k] = b[i][3];
            width[k] = Math.max(b[i][1] - b[i][0], b[i][5] - b[i][4]);
        }
        return new PaperStack(names, base, top, width);
    }

    /** Geometría (px, espacio del modelo) de las capas de papel, ordenadas de la base al tip. Solo floats y nombres. */
    private static final class PaperStack {
        final String[] names;
        final float[] base;
        final float[] top;
        final float[] width;
        final float minBase;
        final float maxTop;

        PaperStack(String[] names, float[] base, float[] top, float[] width) {
            this.names = names;
            this.base = base;
            this.top = top;
            this.width = width;
            float lo = Float.POSITIVE_INFINITY;
            float hi = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < names.length; ++i) {
                lo = Math.min(lo, base[i]);
                hi = Math.max(hi, top[i]);
            }
            this.minBase = names.length == 0 ? 0.0f : lo;
            this.maxTop = names.length == 0 ? 0.0f : hi;
        }

        int layers() {
            return this.names.length;
        }

        int indexOf(String name) {
            for (int i = 0; i < this.names.length; ++i) {
                if (this.names[i].equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        /** Altura (px) hasta la que llega el papel vivo. Con una sola capa es base + p * altura. */
        float liveTop(float progress) {
            return this.minBase + progress * (this.maxTop - this.minBase);
        }

        /** 1 = capa entera, 0 = consumida, intermedio = la capa que arde ahora. */
        float scaleOf(int i, float liveTop) {
            float h = this.top[i] - this.base[i];
            if (h <= 1.0E-4f) {
                return liveTop > this.base[i] ? 1.0f : 0.0f;
            }
            return Mth.clamp((liveTop - this.base[i]) / h, 0.0f, 1.0f);
        }

        /** Capa viva más alta, la que arde ahora (la 0 nunca se da por consumida). */
        int liveIndex(float liveTop) {
            for (int i = this.names.length - 1; i > 0; --i) {
                if (this.scaleOf(i, liveTop) > HIDE_BELOW) {
                    return i;
                }
            }
            return 0;
        }
    }

    // ------------------------------------------------------------------ tip

    private static int applyTip(GeoBone bone, ISmokableItem item, ItemStack stack, @Nullable Level level, float partialTick, boolean isReRender, int colour, int glowTintRgb, float glowTintMix) {
        if (bone.getCubes().isEmpty()) {
            return colour; // grupo contenedor
        }
        if (!item.isTipVisible(stack)) {
            bone.setScaleX(0.0f);
            bone.setScaleY(0.0f);
            bone.setScaleZ(0.0f);
            return colour;
        }
        float p = Mth.clamp((float)item.burnProgress(stack, level), (float)0.0f, (float)1.0f);
        float fit = 1.0f;
        PaperStack paper = stackFor(bone);
        if (paper.layers() > 0) {
            float live = paper.liveTop(p);
            bone.setPosY(live - paper.maxTop); // pegado al final del papel vivo
            if (paper.layers() > 1) {
                // con varias capas el tip toma la anchura de la capa que arde ahora
                float tipWidth = tipWidth(bone);
                float liveWidth = paper.width[paper.liveIndex(live)];
                if (tipWidth > 0.0f && liveWidth > 0.0f) {
                    fit = liveWidth / tipWidth;
                }
            }
        }
        EmberState ember = item.emberState(stack);
        float now = level == null ? partialTick : (float)level.getGameTime() + partialTick;
        float glow = Mth.clamp((float)ember.level(now), (float)0.0f, (float)1.0f);
        glow = glow * glow * (3.0f - 2.0f * glow);
        //float pulse = 1.0f + 0.15f * glow * (Mth.sin((float)(now * 0.9f)) + 0.6f * Mth.sin((float)(now * 2.3f + 1.7f)));

        // Mth.sin usa una tabla indexada por (int)(x*10430.378) y satura con x > ~205887:
// con gameTime absoluto (cientos de miles de ticks) devolvía una constante.
// Fase en double y seno de java.lang.Math.
        double phase = level == null ? partialTick : level.getGameTime() + (double) partialTick;
        float wobble = (float) (Math.sin(phase * 0.9) + 0.6 * Math.sin(phase * 2.3 + 1.7));
        float pulse = 1.0f + 0.02f * glow * wobble; //Cambiar para el efecto del tip

        if (ember.smoking() || ember.levelAtChange() > 0.0f) {
            /*System.out.println("tip smoking=" + ember.smoking() + " changeTick=" + ember.changeTick()
                    + " levelAtChange=" + ember.levelAtChange() + " now=" + now
                    + " lvl=" + ember.level(now) + " levelNull=" + (level == null) + " reRender=" + isReRender);*/
        }        bone.setScaleX(pulse * fit);


        bone.setScaleY(pulse);
        bone.setScaleZ(pulse * fit);
        float charLevel = Math.max(ember.charLevel(now), ember.litTick() < 0L && p < 1.0f ? 1.0f : 0.0f);
        return isReRender ? glowColour(colour, glow, glowTintRgb, glowTintMix) : charColour(colour, charLevel);
    }

    private static float tipWidth(GeoBone tip) {
        Float cached = TIP_WIDTH.get(tip);
        if (cached == null) {
            if (TIP_WIDTH.size() >= CACHE_LIMIT) {
                TIP_WIDTH.clear();
            }
            float[] b = bounds(tip);
            cached = Float.valueOf(Math.max(b[1] - b[0], b[5] - b[4]));
            TIP_WIDTH.put(tip, cached);
        }
        return cached.floatValue();
    }

    /** {minX, maxX, minY, maxY, minZ, maxZ} en px (espacio del modelo) de todos los cubos del hueso. */
    private static float[] bounds(GeoBone bone) {
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    float x = vertex.position().x() * 16.0f;
                    float y = vertex.position().y() * 16.0f;
                    float z = vertex.position().z() * 16.0f;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }
        return new float[]{minX, maxX, minY, maxY, minZ, maxZ};
    }

    // ------------------------------------------------------------------ color (sin cambios)

    private static int glowColour(int argb, float glow, int tintRgb, float tintMix) {
        int alpha = Math.round((float)(argb >>> 24 & 0xFF) * glow);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        float hottest = Math.max(sourceRed, Math.max(sourceGreen, sourceBlue));
        float warmRed = Math.max((float)sourceRed, hottest * 0.85f);
        float greenLimit = warmRed * Mth.lerp((float)glow, (float)0.18f, (float)0.62f);
        int red = Math.round(warmRed);
        int green = Math.round(Math.min(Math.max((float)sourceGreen, hottest * 0.12f), greenLimit));
        int blue = Math.round(Math.min((float)sourceBlue, warmRed * 0.06f));
        if (tintMix > 0.0f) {
            float k = hottest / 255.0f;
            red = Math.round(Mth.lerp((float)tintMix, (float)red, (float)((float)(tintRgb >>> 16 & 0xFF) * k)));
            green = Math.round(Mth.lerp((float)tintMix, (float)green, (float)((float)(tintRgb >>> 8 & 0xFF) * k)));
            blue = Math.round(Mth.lerp((float)tintMix, (float)blue, (float)((float)(tintRgb & 0xFF) * k)));
        }
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int charColour(int argb, float charLevel) {
        float amount = Mth.clamp((float)charLevel, (float)0.0f, (float)1.0f);
        int sourceRed = argb >>> 16 & 0xFF;
        int sourceGreen = argb >>> 8 & 0xFF;
        int sourceBlue = argb & 0xFF;
        float luminance = (float)sourceRed * 0.2126f + (float)sourceGreen * 0.7152f + (float)sourceBlue * 0.0722f;
        float charcoal = luminance * 0.28f;
        int red = Math.round(Mth.lerp((float)amount, (float)sourceRed, (float)charcoal));
        int green = Math.round(Mth.lerp((float)amount, (float)sourceGreen, (float)charcoal));
        int blue = Math.round(Mth.lerp((float)amount, (float)sourceBlue, (float)charcoal));
        return argb & 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
