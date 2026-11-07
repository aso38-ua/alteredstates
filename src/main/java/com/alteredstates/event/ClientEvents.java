package com.alteredstates.event;

import com.alteredstates.client.render.DryingRackRenderer;
import com.alteredstates.client.render.CuringJarRenderer;
import com.alteredstates.client.render.BongRenderer;
import com.alteredstates.client.render.IndustrialCauldronRenderer;
import com.alteredstates.item.CigaretteItem;
import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import com.alteredstates.client.render.LabFurnaceRenderer;

@EventBusSubscriber(modid = "alteredstates", value = Dist.CLIENT)public class ClientEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // 🔍 Si el ítem tiene el componente guardado internamente, lo mostramos
        if (stack.has(ModDataComponentTypes.QUALITY.get())) {
            if (!(stack.getItem() instanceof com.alteredstates.item.RolledJointItem) && !(stack.getItem() instanceof com.alteredstates.item.CigaretteItem)) {
                int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                Component qualityName = switch (quality) {
                    case 0 -> Component.translatable("tooltip.alteredstates.quality.bad");
                    case 1 -> Component.translatable("tooltip.alteredstates.quality.regular");
                    case 2 -> Component.translatable("tooltip.alteredstates.quality.normal");
                    case 3 -> Component.translatable("tooltip.alteredstates.quality.good");
                    default -> Component.translatable("tooltip.alteredstates.quality.premium");
                };
                event.getToolTip().add(Component.literal("⭐ ").append(Component.translatable("tooltip.alteredstates.quality_format", qualityName)).withStyle(ChatFormatting.GOLD));
            }
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

        event.registerBlockEntityRenderer(ModBlockEntities.INDUSTRIAL_CAULDRON.get(), IndustrialCauldronRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.LAB_FURNACE.get(), LabFurnaceRenderer::new);
    }

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> { // ItemProperties#register is not threadsafe, so we need to call it on the main thread
            ItemProperties.register(
                    // The item to apply the property to.
                    ModItems.CIGARETTE.get(),
                    // The id of the property.
                    ResourceLocation.fromNamespaceAndPath("alteredstates", "visual_state"),
                    // A reference to a method that calculates the override value.
                    // Parameters are the used item stack, the level context, the player using the item,
                    // and a random seed you can use.
                    (stack, level, player, seed) -> {
                        if (stack.getItem() instanceof CigaretteItem cigarette) {
                            //System.out.println("NUUUUUUUUUUUUUMERO ======= " + cigarette.getVisualStateIndex(stack));
                            return (float) cigarette.getVisualStateIndex(stack, level);
                        }
                        return 0.0f;
                    }
            );
        });
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().is(CigaretteItem.CIGARETTE_DAMAGE)) {
            AdvancementHolder adv = player.server.getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath("alteredstates", "vicious"));
            if (adv != null) {
                player.getAdvancements().award(adv, "death");
            }
        }
    }
}