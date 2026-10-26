package com.alteredstates.client.render;

import com.alteredstates.block.entity.IndustrialCauldronBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Dibuja el líquido y los ítems flotando dentro del caldero (sin GUI). Rota con FACING. */
public class IndustrialCauldronRenderer implements BlockEntityRenderer<IndustrialCauldronBlockEntity> {

    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final float MIN_Y = 0.30F;   // suelo interior del caldero
    private static final float MAX_Y = 0.92F;   // borde de líquido lleno
    private static final float EDGE_MIN = 2F / 16F;
    private static final float EDGE_MAX = 14F / 16F;

    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public IndustrialCauldronRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(IndustrialCauldronBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        int liquidMb = Math.max(be.getWaterMb(), be.getGasolineMb());
        boolean hasOutput = !be.getOutput().isEmpty();
        List<ItemStack> items = be.getIngredientsFlat();
        if (liquidMb <= 0 && items.isEmpty() && !hasOutput) return;

        float fill = Math.min(1.0F, liquidMb / 1000.0F);
        float surfaceY = MIN_Y + (MAX_Y - MIN_Y) * fill;
        long time = be.getLevel() == null ? 0 : be.getLevel().getGameTime();

        poseStack.pushPose();
        BlockRotationHelper.applyFacing(poseStack, be.getBlockState());

        // ── Líquido ──
        if (liquidMb > 0) {
            renderLiquid(poseStack, buffer, light, surfaceY, be.getGasolineMb() > 0);
        }

        // ── Ítems flotando en círculo ──
        int n = items.size();
        for (int i = 0; i < n; i++) {
            float angle = (float) (i * (Math.PI * 2.0D / Math.max(n, 1)));
            float bob = (float) Math.sin((time + partialTick) / 12.0F + i) * 0.012F;
            this.random.setSeed(be.getBlockPos().asLong() + i * 31L);
            float radius = 0.20F + this.random.nextFloat() * 0.08F;

            poseStack.pushPose();
            poseStack.translate(0.5D + Math.cos(angle) * radius, surfaceY + 0.02F + bob, 0.5D + Math.sin(angle) * radius);
            poseStack.mulPose(Axis.XP.rotationDegrees(90F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.random.nextFloat() * 360F));
            poseStack.scale(0.32F, 0.32F, 0.32F);
            renderItem(be, items.get(i), poseStack, buffer, light, overlay);
            poseStack.popPose();
        }

        // ── Resultado listo: gira lentamente sobre el líquido ──
        if (hasOutput) {
            poseStack.pushPose();
            poseStack.translate(0.5D, surfaceY + 0.18F + Math.sin((time + partialTick) / 14.0F) * 0.02F, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees((time + partialTick) * 2.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            renderItem(be, be.getOutput(), poseStack, buffer, light, overlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderItem(IndustrialCauldronBlockEntity be, ItemStack stack, PoseStack poseStack,
                            MultiBufferSource buffer, int light, int overlay) {
        BakedModel model = this.itemRenderer.getModel(stack, be.getLevel(), null, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, light, overlay, model);
    }

    private void renderLiquid(PoseStack poseStack, MultiBufferSource buffer, int light, float y, boolean gasoline) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(WATER_STILL);

        int r, g, b, a;
        if (gasoline) { r = 0x2B; g = 0x20; b = 0x12; a = 245; }  // marrón casi negro
        else          { r = 0x3F; g = 0x76; b = 0xE4; a = 215; }  // agua

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();

        // cara superior (y su reverso, para que se vea desde cualquier lado)
        quad(vc, pose, EDGE_MIN, y, EDGE_MAX, EDGE_MAX, y, EDGE_MAX, EDGE_MAX, y, EDGE_MIN, EDGE_MIN, y, EDGE_MIN,
                u0, u1, v0, v1, r, g, b, a, light, 1F);
        quad(vc, pose, EDGE_MIN, y, EDGE_MIN, EDGE_MAX, y, EDGE_MIN, EDGE_MAX, y, EDGE_MAX, EDGE_MIN, y, EDGE_MAX,
                u0, u1, v0, v1, r, g, b, a, light, -1F);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float u0, float u1, float v0, float v1,
                             int r, int g, int b, int a, int light, float ny) {
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u0, v1).setLight(light).setNormal(pose, 0, ny, 0);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u1, v1).setLight(light).setNormal(pose, 0, ny, 0);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u1, v0).setLight(light).setNormal(pose, 0, ny, 0);
        vc.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(u0, v0).setLight(light).setNormal(pose, 0, ny, 0);
    }
}
