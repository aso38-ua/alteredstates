package com.alteredstates.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.world.item.ItemStack;

/**
 * Fija DÓNDE aparece en pantalla el origen del modelo de Blockbench en primera persona, y recoge los
 * brazos (ArmAnchor) que hay que dibujar después del ítem.
 *
 * Convención de autoría (la misma de Immersive Eating): los huesos se llaman rightArm / leftArm /
 * camera, el brazo derecho está en X negativo y el modelo se anima tal cual en Blockbench. Si el
 * origen está bien calibrado, lo que ves en Blockbench es lo que ves en el juego, sin rotaciones
 * sueltas en ningún renderer.
 *
 * Una instancia por renderer (guarda los brazos del frame actual). Lo estático es la calibración.
 *
 * PASO 1: solo mano derecha principal. Zurdos = paso 1b (espejo del modelo y de los vértices).
 */
public final class FirstPersonRig {

    /**
     * CALIBRACIÓN: desplazamiento respecto al ancla de mano de vanilla.
     * Valores de partida = los que usa Immersive Eating (tomados de su mod, ver notas). Son mutables
     * a propósito para poder ajustarlos en caliente con un comando de desarrollo, sin reiniciar.
     */
    public static volatile float originX = -1.05f;
    public static volatile float originY = -0.35f;
    public static volatile float originZ = -0.80f;

    /** Los renderers que quieran el rig se anuncian con esta interfaz; el mixin solo actúa sobre ellos. */
    public interface Host {
        /** El mixin solo desvía el render de este ítem si devuelve true. */
        default boolean usesFirstPersonRig(ItemStack stack) {
            return true;
        }
    }

    private final List<ArmAnchor.Captured> captured = new ArrayList<>();

    // ---------------------------------------------------------------- contexto

    /** Paso 1: solo FIRST_PERSON_RIGHT_HAND. El paso 1b añadirá LEFT_HAND. */
    public static boolean isFirstPerson(ItemDisplayContext ctx) {
        return ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    /** ¿Este contexto corresponde a la mano principal del jugador (y no a la secundaria)? */
    public static boolean isMainHandContext(ItemDisplayContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return ctx == (mc.player.getMainArm() == HumanoidArm.LEFT
                ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
    }

    /** Lo llama el mixin justo antes de renderByItem. */
    public static void applyOrigin(PoseStack poseStack, ItemDisplayContext ctx) {
        poseStack.translate(originX, originY, originZ);
    }

    // ---------------------------------------------------------------- ciclo de un frame

    /** Al empezar renderByItem. */
    public void begin() {
        captured.clear();
    }

    /**
     * En renderRecursively, tras RenderUtil.prepMatrixForBone: si el hueso es un brazo, lo captura y
     * devuelve true (el caller NO debe dibujar los cubos de ese hueso, pero sí sus hijos).
     */
    public boolean captureIfArm(PoseStack poseStack, GeoBone bone) {
        if (!ArmAnchor.isArmBone(bone)) return false;
        ArmAnchor.Captured c = ArmAnchor.capture(poseStack, bone, false, new Matrix4f());
        if (c != null) captured.add(c);
        return true;
    }

    /** Al terminar renderByItem (las matrices capturadas son absolutas: el orden respecto al ítem da igual). */
    public void flush(MultiBufferSource buffer, int packedLight) {
        for (ArmAnchor.Captured c : captured) ArmAnchor.render(c, buffer, packedLight, false);
        captured.clear();
    }
}
