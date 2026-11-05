package com.alteredstates.client.render;

import com.alteredstates.item.CigaretteItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CigaretteModel extends GeoModel<CigaretteItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            "alteredstates", "geo/item/cigarette.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "alteredstates", "textures/item/cigarette.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            "alteredstates", "animations/item/cigarette.animation.json");

    @Override
    public ResourceLocation getModelResource(CigaretteItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CigaretteItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CigaretteItem animatable) {
        return ANIMATION;
    }
}
