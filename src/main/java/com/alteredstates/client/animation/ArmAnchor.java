package com.alteredstates.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

/**
 * Convierte un hueso "rightArm"/"leftArm" del geo en un brazo REAL del jugador (con su skin).
 *
 * Idea: en Blockbench el brazo es solo un cubo de 4x12x4 px que sirve de marcador. El renderer NO
 * dibuja ese cubo: captura la matriz del PoseStack en ese hueso (que ya lleva la animación) y calcula
 * la matriz que lleva el modelo del brazo vanilla (4x12x4, centro conocido) a la posición, rotación y
 * escala del cubo marcador. Luego se llama a PlayerRenderer#renderRightHand/LeftHand con esa matriz.
 *
 * Sin estado: todo es estático y no depende de ningún ítem.
 */
public final class ArmAnchor {
    private static final float PX = 1.0f / 16.0f;

    /** Centro del cubo del brazo vanilla respecto al origen de su ModelPart (px): derecho (-6,6,0), izquierdo (6,6,0). */
    private static final Vector3f VANILLA_CENTER_RIGHT = new Vector3f(-6.0f, 6.0f, 0.0f).mul(PX);
    private static final Vector3f VANILLA_CENTER_LEFT = new Vector3f(6.0f, 6.0f, 0.0f).mul(PX);

    /** Brazo ya resuelto: matriz absoluta (incluye todo el PoseStack hasta el hueso) lista para dibujar. */
    public record Captured(HumanoidArm arm, Matrix4f pose, Matrix3f normal) {}

    private ArmAnchor() {}

    // ---------------------------------------------------------------- detección

    /** Nombres admitidos (sin distinguir mayúsculas ni "_"): rightArm, leftArm. */
    public static boolean isArmBone(GeoBone bone) {
        return armOf(bone) != null;
    }

    public static HumanoidArm armOf(GeoBone bone) {
        String n = bone.getName().replace("_", "").replace(" ", "").toLowerCase();
        return switch (n) {
            case "rightarm" -> HumanoidArm.RIGHT;
            case "leftarm" -> HumanoidArm.LEFT;
            default -> null;
        };
    }

    // ---------------------------------------------------------------- captura

    /**
     * @param poseStack  pila JUSTO tras RenderUtil.prepMatrixForBone(bone): lleva animación y jerarquía.
     * @param mirrored   true si el modelo se está dibujando espejado (mano izquierda principal): se
     *                   dibuja el brazo contrario. (Reservado para el paso de zurdos; ahora siempre false.)
     * @param localOffset desplazamiento extra en espacio del hueso (p. ej. corrección de skin slim);
     *                   identidad por defecto.
     * @return el brazo capturado o null si el hueso no tiene cubo.
     */
    public static Captured capture(PoseStack poseStack, GeoBone bone, boolean mirrored, Matrix4f localOffset) {
        HumanoidArm arm = armOf(bone);
        if (arm == null || bone.getCubes().isEmpty()) return null;
        HumanoidArm rendered = mirrored ? opposite(arm) : arm;
        GeoCube cube = bone.getCubes().get(0);

        Vector3f center = centerOf(cube);
        if (center == null) return null;

        // Rotación/pivote propios del cubo (Blockbench permite rotar el cubo dentro del hueso).
        Vec3 pivot = cube.pivot();
        Vec3 rot = cube.rotation();
        Matrix4f cubeTransform = new Matrix4f()
                .translate((float) pivot.x / 16.0f, (float) pivot.y / 16.0f, (float) pivot.z / 16.0f)
                .rotateZ((float) rot.z).rotateY((float) rot.y).rotateX((float) rot.x)
                .translate((float) -pivot.x / 16.0f, (float) -pivot.y / 16.0f, (float) -pivot.z / 16.0f);
        Vector3f target = cubeTransform.transformPosition(center, new Vector3f());

        Vector3f vc = rendered == HumanoidArm.RIGHT ? VANILLA_CENTER_RIGHT : VANILLA_CENTER_LEFT;
        Vec3 size = cube.size();

        // vanilla -> cubo marcador:  T(destino) * R(cubo) * Rz(pi) * S(tamaño/vanilla) * T(-centro vanilla)
        // Rz(pi): el ModelPart vanilla tiene +Y hacia abajo; el geo tiene +Y hacia arriba.
        Matrix4f pose = new Matrix4f((Matrix4fc) poseStack.last().pose())
                .mul(localOffset)
                .translate(target)
                .rotateZ((float) rot.z).rotateY((float) rot.y).rotateX((float) rot.x)
                .rotateZ((float) Math.PI)
                .scale(safe(size.x, 4.0f), safe(size.y, 12.0f), safe(size.z, 4.0f))
                .translate(-vc.x(), -vc.y(), -vc.z());

        return new Captured(rendered, pose, new Matrix3f((Matrix4fc) pose).normal());
    }

    // ---------------------------------------------------------------- dibujo

    public static void render(Captured c, MultiBufferSource buffer, int packedLight, boolean mirrored) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isInvisible()) return;

        PoseStack ps = new PoseStack();
        ps.last().pose().set((Matrix4fc) c.pose());
        ps.last().normal().set((Matrix3fc) c.normal());

        MultiBufferSource src = mirrored ? new MirroredSource(buffer, player.getSkin().texture()) : buffer;
        PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        if (c.arm() == HumanoidArm.RIGHT) renderer.renderRightHand(ps, src, packedLight, player);
        else renderer.renderLeftHand(ps, src, packedLight, player);
    }

    /**
     * Al espejar el modelo se invierte el orden de los vértices y el culling oculta las caras. La capa
     * "entitySolid" de la skin se sustituye por "entityTranslucent" (sin culling). Solo se usa si mirrored.
     */
    private record MirroredSource(MultiBufferSource delegate, ResourceLocation skin) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType type) {
            if (type == RenderType.entitySolid(skin)) return delegate.getBuffer(RenderType.entityTranslucent(skin));
            return delegate.getBuffer(type);
        }
    }

    // ---------------------------------------------------------------- utilidades

    private static HumanoidArm opposite(HumanoidArm a) {
        return a == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static float safe(double size, float vanilla) {
        return size > 0.0 ? (float) size / vanilla : 1.0f;
    }

    /** Centro de la caja envolvente de los vértices del cubo (en unidades de bloque, ya en espacio del hueso). */
    private static Vector3f centerOf(GeoCube cube) {
        float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
        float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (GeoQuad quad : cube.quads()) {
            if (quad == null) continue;
            for (GeoVertex v : quad.vertices()) {
                Vector3f p = v.position();
                minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
                minZ = Math.min(minZ, p.z()); maxZ = Math.max(maxZ, p.z());
            }
        }
        if (Float.isInfinite(minX)) return null;
        return new Vector3f((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f);
    }
}
