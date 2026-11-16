package com.alteredstates.client.render;

import com.alteredstates.item.SmokableItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/** Un único modelo para todos los fumables: lee qué archivos usar de animatable.visuals(). */
public class SmokableModel extends GeoModel<SmokableItem> {

    @Override
    public ResourceLocation getModelResource(SmokableItem animatable) {
        return animatable.visuals().model();
    }

    @Override
    public ResourceLocation getModelResource(SmokableItem animatable, @Nullable GeoRenderer<SmokableItem> renderer) {
        if (renderer instanceof SmokableRenderer smokableRenderer && smokableRenderer.isFirstPersonRender()) {
            return animatable.visuals().fpModel();
        }
        return animatable.visuals().model();
    }

    @Override
    public ResourceLocation getTextureResource(SmokableItem animatable) {
        return animatable.visuals().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(SmokableItem animatable) {
        return animatable.visuals().fpAnimation();
    }
}
