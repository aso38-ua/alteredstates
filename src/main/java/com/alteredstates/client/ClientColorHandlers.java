package com.alteredstates.client;

import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Tinte de la coca segun bioma (y estacion, si Serene/Ecliptic cambian el color del follaje).
 * Las texturas estan en gris; esto las colorea.
 */
@EventBusSubscriber(modid = "alteredstates", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientColorHandlers {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    /** Verde coca fijo con el que se mezcla el follaje del bioma. */
    private static final int BASE = 0x5EA03C;
    /** 0 = solo color del bioma, 1 = siempre el mismo verde. Sube para que varie menos. */
    private static final float BASE_MIX = 0.40F;

    private static int mix(int biome) {
        int r = (int) (((biome >> 16) & 255) * (1 - BASE_MIX) + ((BASE >> 16) & 255) * BASE_MIX);
        int g = (int) (((biome >> 8) & 255) * (1 - BASE_MIX) + ((BASE >> 8) & 255) * BASE_MIX);
        int b = (int) ((biome & 255) * (1 - BASE_MIX) + (BASE & 255) * BASE_MIX);
        return (r << 16) | (g << 8) | b;
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) ->
                        tint == 0 ? (level != null && pos != null ? mix(BiomeColors.getAverageFoliageColor(level, pos)) : BASE) : -1,
                ModBlocks.COCA_CROP.get(), ModBlocks.WILD_COCA.get());
        LOGGER.info("[AlteredStates] Coca block color handlers registered");
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        // OJO: en 1.21 el item color necesita canal alfa (0xFF000000), si no el item sale invisible
        event.register((stack, tint) -> tint == 0 ? (0xFF000000 | mix(FoliageColor.getDefaultColor())) : -1,
                ModItems.COCA_LEAF.get());
        LOGGER.info("[AlteredStates] Coca item color handler registered");
    }
}
