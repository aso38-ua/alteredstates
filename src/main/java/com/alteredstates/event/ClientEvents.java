package com.alteredstates.event;

import com.alteredstates.client.render.DryingRackRenderer;
import com.alteredstates.client.render.CuringJarRenderer;
import com.alteredstates.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // El del secadero que ya funcionaba
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);

        //Registramos el renderizador dinámico del Tarro
        event.registerBlockEntityRenderer(ModBlockEntities.CURING_JAR.get(), CuringJarRenderer::new);
    }
}