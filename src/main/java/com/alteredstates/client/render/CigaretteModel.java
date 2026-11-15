package com.alteredstates.client.render;

import com.alteredstates.item.CigaretteItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/** Un único modelo para todos los fumables: lee qué archivos usar de animatable.visuals(). */
public class CigaretteModel extends GeoModel<CigaretteItem> {

    @Override
    public ResourceLocation getModelResource(CigaretteItem animatable) {
        return animatable.visuals().model();
    }

    @Override
    public ResourceLocation getModelResource(CigaretteItem animatable, @Nullable GeoRenderer<CigaretteItem> renderer) {
        if (renderer instanceof CigaretteRenderer cigaretteRenderer && cigaretteRenderer.isFirstPersonRender()) {
            return animatable.visuals().fpModel();
        }
        return animatable.visuals().model();
    }

    @Override
    public ResourceLocation getTextureResource(CigaretteItem animatable) {
        return animatable.visuals().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(CigaretteItem animatable) {
        return animatable.visuals().fpAnimation();
    }
}
