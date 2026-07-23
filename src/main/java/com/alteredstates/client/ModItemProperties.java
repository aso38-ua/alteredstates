package com.alteredstates.client;

import com.alteredstates.AlteredStates;
import com.alteredstates.item.CigaretteItem;
import com.alteredstates.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ModItemProperties {
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.CIGARETTE.get(),
                    ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "visual_state"),
                    (stack, level, entity, seed) -> {
                        if (stack.getItem() instanceof CigaretteItem cigarette) {
                            return cigarette.getVisualStateIndex(stack, level);
                        }
                        return 0.0F;
                    }
            );
        });
    }
}
