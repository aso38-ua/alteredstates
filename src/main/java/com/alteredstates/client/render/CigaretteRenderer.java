package com.alteredstates.client.render;

import com.alteredstates.component.EmberState;
import com.alteredstates.item.CigaretteItem;
import com.alteredstates.registry.ModDataComponentTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CigaretteRenderer extends GeoItemRenderer<CigaretteItem> {
    private static final float PAPER_LEN_PX = 30f; // size Y del cubo de paper en tu modelo

    public CigaretteRenderer() {
        super(new CigaretteModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void renderRecursively(PoseStack ps, CigaretteItem item, GeoBone bone, RenderType rt,
                                  MultiBufferSource buf, VertexConsumer vc, boolean isReRender, float partialTick,
                                  int light, int overlay, int colour) {

        ItemStack stack = getCurrentItemStack();
        Level level = Minecraft.getInstance().level;
        float p = Mth.clamp(CigaretteItem.getBurnProgress(stack, level), 0f, 1f);

        switch (bone.getName()) {
            case "paper" -> bone.setScaleY(Math.max(p, 0.001f));
            case "tip" -> {
                bone.setPosY(-PAPER_LEN_PX * (1f - p));
                bone.setHidden(!(CigaretteItem.isLit(stack, level) || p < 1f));
                if (isReRender) { // pasada de la capa glow: aquí se atenúa la brasa
                    colour = withAlpha(colour, emberLevel(stack, partialTick));
                }
            }
        }
        super.renderRecursively(ps, item, bone, rt, buf, vc, isReRender,
                partialTick, light, overlay, colour);
    }

    private static float emberLevel(ItemStack stack, float partialTick) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0f;
        return stack.getOrDefault(ModDataComponentTypes.EMBER.get(), EmberState.OFF)
                .level(level.getGameTime() + partialTick);
    }

    private static int withAlpha(int argb, float f) {
        int a = Math.round(((argb >>> 24) & 0xFF) * Mth.clamp(f, 0f, 1f));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
