package com.alteredstates;

import com.alteredstates.client.PalSetup;
import com.alteredstates.client.animation.FirstPersonRig;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AlteredStates.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = AlteredStates.MOD_ID, value = Dist.CLIENT)
public class AlteredStatesClient {
    public AlteredStatesClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        AlteredStates.LOGGER.info("HELLO FROM CLIENT SETUP");
        AlteredStates.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        /*event.enqueueWork(() -> {
            AlteredStates.LOGGER.info("[PAL debug] registrando capa");
            PalSetup.register();
        });*/
    }
    @SubscribeEvent
    static void cmds(RegisterClientCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("fprig")
                .then(Commands.argument("x", FloatArgumentType.floatArg())
                        .then(Commands.argument("y", FloatArgumentType.floatArg())
                                .then(Commands.argument("z", FloatArgumentType.floatArg()).executes(c -> {
                                    FirstPersonRig.originX = FloatArgumentType.getFloat(c, "x");
                                    FirstPersonRig.originY = FloatArgumentType.getFloat(c, "y");
                                    FirstPersonRig.originZ = FloatArgumentType.getFloat(c, "z");
                                    return 1; })))));
    }
}
