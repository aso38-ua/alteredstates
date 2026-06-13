package com.alteredstates.event;

import com.alteredstates.client.render.DryingRackRenderer;
import com.alteredstates.client.render.CuringJarRenderer;
import com.alteredstates.client.render.BongRenderer;
import com.alteredstates.registry.ModBlockEntities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // 🔍 Si el ítem tiene el componente guardado internamente, lo mostramos
        if (stack.has(ModDataComponentTypes.QUALITY.get())) {
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            // Añade "⭐ Calidad: X" en color dorado al final de la descripción
            event.getToolTip().add(Component.literal("⭐ Calidad: " + quality).withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // El del secadero que ya funcionaba
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);

        //Registramos el renderizador dinámico del Tarro
        event.registerBlockEntityRenderer(ModBlockEntities.CURING_JAR.get(), CuringJarRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.ROLLING_TRAY.get(), com.alteredstates.client.render.RollingTrayRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.BONG.get(), BongRenderer::new);
    }
}