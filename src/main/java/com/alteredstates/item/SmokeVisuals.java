package com.alteredstates.item;

import net.minecraft.resources.ResourceLocation;

/**
 * TODO lo visual de un ítem fumable, en un solo sitio. Es lo único que cambia entre un cigarrillo,
 * un cigarrillo B, un puro o una pipa; el renderer, el modelo y la capa de brillo lo leen del ítem.
 *
 * Convención de archivos a partir del id (todo bajo assets/alteredstates/):
 *   geo/item/<id>.geo.json            modelo normal (tercera persona, inventario, cabeza)
 *   geo/item/<id>_fp.geo.json         modelo de primera persona (rig + este objeto)
 *   textures/item/<id>.png            textura
 *   textures/item/<id>_glowmask.png   máscara del brillo de la brasa
 *   animations/item/cigarette_fp.animation.json   animación de primera persona (compartida por defecto)
 *   player_animations/cigarette_*.json            animación de tercera persona (compartida por defecto)
 *
 * of(id) reutiliza las animaciones del cigarrillo. Para animaciones propias:
 *   SmokeVisuals.of("cigar_torito").withFpAnimation("cigar_fp").withThirdPerson(SmokeAnimation.of("cigar", 9, 9))
 *
 * Brillo de la brasa con color propio (p. ej. verde para el joint):
 *   SmokeVisuals.of("joint").withGlowTint(0x8CFF3C, 0.7f)    // color RGB y cuánto se mezcla (0 = brasa normal, 1 = solo ese color)
 *
 * NONE = "este ítem todavía no tiene modelo geo": el renderer no lo toca y sigue el camino vanilla.
 * Úsalo para pipas u otros ítems hasta que existan sus archivos.
 *
 * Clase común (sin imports de cliente) porque la usa el ítem.
 */
public final class SmokeVisuals {
    private static final String MOD = "alteredstates";

    public static final ResourceLocation DEFAULT_FP_ANIMATION = rl("animations/item/cigarette_fp.animation.json");
    public static final SmokeVisuals CIGARETTE = of("cigarette");
    public static final SmokeVisuals NONE = new SmokeVisuals("none", DEFAULT_FP_ANIMATION, FpClips.DEFAULT, SmokeAnimation.SMOKING, false, 0, 0.0f);

    private final String id;
    private final ResourceLocation model;
    private final ResourceLocation fpModel;
    private final ResourceLocation texture;
    private final ResourceLocation glowTexture;
    private final ResourceLocation fpAnimation;
    private final FpClips clips;
    private final SmokeAnimation thirdPerson;
    private final boolean custom;
    private final int glowTintRgb;
    private final float glowTintMix;

    private SmokeVisuals(String id, ResourceLocation fpAnimation, FpClips clips, SmokeAnimation thirdPerson, boolean custom,
                         int glowTintRgb, float glowTintMix) {
        this.id = id;
        this.model = rl("geo/item/" + id + ".geo.json");
        this.fpModel = rl("geo/item/" + id + "_fp.geo.json");
        this.texture = rl("textures/item/" + id + ".png");
        this.glowTexture = rl("textures/item/" + id + "_glowmask.png");
        this.fpAnimation = fpAnimation;
        this.clips = clips;
        this.thirdPerson = thirdPerson;
        this.custom = custom;
        this.glowTintRgb = glowTintRgb;
        this.glowTintMix = glowTintMix;
    }

    /** Visuales propios (modelo, textura, glowmask) con las animaciones por defecto del cigarrillo. */
    public static SmokeVisuals of(String id) {
        return new SmokeVisuals(id, DEFAULT_FP_ANIMATION, FpClips.DEFAULT, SmokeAnimation.CIGARETTE, true, 0, 0.0f);
    }

    /** Otro archivo de animación de primera persona: "cigar_fp" -> animations/item/cigar_fp.animation.json */
    public SmokeVisuals withFpAnimation(String fileName) {
        return new SmokeVisuals(id, rl("animations/item/" + fileName + ".animation.json"), clips, thirdPerson, custom, glowTintRgb, glowTintMix);
    }

    /** Otros nombres de clip dentro del archivo (si no son idle/raise/hold/lower). */
    public SmokeVisuals withClips(FpClips newClips) {
        return new SmokeVisuals(id, fpAnimation, newClips, thirdPerson, custom, glowTintRgb, glowTintMix);
    }

    /** Otra animación de tercera persona (PAL). */
    public SmokeVisuals withThirdPerson(SmokeAnimation animation) {
        return new SmokeVisuals(id, fpAnimation, clips, animation, custom, glowTintRgb, glowTintMix);
    }

    /** Color del brillo de la brasa. rgb = 0xRRGGBB; mix 0 = brasa normal, 1 = solo ese color. */
    public SmokeVisuals withGlowTint(int rgb, float mix) {
        return new SmokeVisuals(id, fpAnimation, clips, thirdPerson, custom, rgb, mix);
    }

    public String id() { return id; }
    public ResourceLocation model() { return model; }
    public ResourceLocation fpModel() { return fpModel; }
    public ResourceLocation texture() { return texture; }
    public ResourceLocation glowTexture() { return glowTexture; }
    public ResourceLocation fpAnimation() { return fpAnimation; }
    public FpClips clips() { return clips; }
    public SmokeAnimation thirdPerson() { return thirdPerson; }
    public int glowTintRgb() { return glowTintRgb; }
    public float glowTintMix() { return glowTintMix; }
    /** false solo en NONE. */
    public boolean isCustom() { return custom; }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD, path);
    }
}
