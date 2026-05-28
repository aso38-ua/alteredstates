package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlteredStates.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALTERED_STATES_TAB =
            CREATIVE_TABS.register("alteredstates_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.alteredstates"))
                            // Icono temporal hasta tener el joint registrado
                            .icon(() -> new ItemStack(ModItems.INDICA_BUDS_FRESH.get()))
                            .displayItems((params, output) -> {
                                // Se populará automáticamente cuando
                                // añadamos items reales al DeferredRegister
                                ModItems.ITEMS.getEntries().forEach(
                                        holder -> output.accept(holder.get())
                                );
                            })
                            .build()
            );
}