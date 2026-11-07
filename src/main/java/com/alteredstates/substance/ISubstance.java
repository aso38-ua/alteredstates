package com.alteredstates.substance;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Interfaz base para cualquier sustancia del mod (cocaína, tabaco, cannabis...).
 * Define el ciclo de vida de consumo, tolerancia, dependencia y abstinencia (mono).
 */
public interface ISubstance {

    String getId();

    Component getDisplayName();

    Component getWarningTooltip();

    void onConsume(ServerPlayer player, ItemStack stack, int quality);

    void tick(ServerPlayer player, SubstanceData data);

    void onMilkDrank(ServerPlayer player, SubstanceData data);

    boolean hasWithdrawal(SubstanceData data, long now);
}

