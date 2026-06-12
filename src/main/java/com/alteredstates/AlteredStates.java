package com.alteredstates;

import com.alteredstates.compat.CompatManager;
import com.alteredstates.compat.FarmersDelightCompat;
import com.alteredstates.compat.fd.FDRecipes;
import com.alteredstates.event.ClientEvents;
import com.alteredstates.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(AlteredStates.MOD_ID)
public class AlteredStates {

    public static final String MOD_ID = "alteredstates";
    public static final Logger LOGGER  = LogUtils.getLogger();

    public AlteredStates(IEventBus modEventBus, ModContainer modContainer) {

        ModDataComponentTypes.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEffects.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        if (CompatManager.FARMERS_DELIGHT) {
            FDRecipes.register(); // Esto registra las recetas custom de FD sin petar si no está
        }

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientEvents::registerRenderers);
        }

        LOGGER.info("[AlteredStates] Initializing — stay chill.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (CompatManager.FARMERS_DELIGHT) {
                FarmersDelightCompat.init();
                LOGGER.info("[AlteredStates] Farmer's Delight compat loaded.");
            }
            // Create y Serene Seasons se registran via sus propios eventos
        });
    }
}