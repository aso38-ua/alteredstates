package com.alteredstates.substance;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/**
 * Lógica de adicción, tolerancia y mono para el tabaco (cigarrillos, puros, pipa).
 * La nicotina genera dependencia rápida; al cabo de medio día sin fumar aparecen
 * fatiga minera y lentitud ("ansiedad / manos temblorosas").
 */
public class TobaccoSubstance implements ISubstance {

    public static final String ID = "tobacco";
    private static final int TOLERANCE_RESET = 96_000;    // 4 días de juego
    private static final int WITHDRAWAL_AFTER = 12_000;   // Medio día sin fumar
    private static final int WITHDRAWAL_MIN_DEP = 25;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("substance.alteredstates.tobacco");
    }

    @Override
    public Component getWarningTooltip() {
        return Component.translatable("tooltip.alteredstates.tobacco.warning")
                .withStyle(ChatFormatting.RED);
    }

    @Override
    public void onConsume(ServerPlayer player, ItemStack stack, int quality) {
        SubstanceData data = SubstanceManager.getOrCreateData(player, ID);
        long now = player.level().getGameTime();

        if (now - data.lastUse > TOLERANCE_RESET) {
            data.tolerance = 0;
        }

        boolean wasInWithdrawal = hasWithdrawal(data, now);

        data.tolerance = Math.min(10, data.tolerance + 1);
        data.dependency = Math.min(100, data.dependency + 8 + quality * 2);
        data.lastUse = now;

        if (wasInWithdrawal) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.displayClientMessage(
                    Component.translatable("message.alteredstates.tobacco.calmed")
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
        }
    }

    @Override
    public boolean hasWithdrawal(SubstanceData data, long now) {
        return data.dependency >= WITHDRAWAL_MIN_DEP && (now - data.lastUse) > WITHDRAWAL_AFTER;
    }

    @Override
    public void tick(ServerPlayer player, SubstanceData data) {
        long now = player.level().getGameTime();

        // Baja la dependencia lentamente si pasa más de 1 día sin consumir
        if (data.dependency > 0 && now - data.lastUse > 24_000 && now % 4800 == 0) {
            data.dependency--;
        }

        if (hasWithdrawal(data, now) && !SubstanceManager.isWithdrawalSuppressed(player)) {
            int severity = 1 + data.dependency / 40; // 1..3

            if (now % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, severity - 1, true, false, false));
                if (severity >= 2) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false, false));
                }
            }

            if (now % 2400 == 0) {
                player.displayClientMessage(
                        Component.translatable("message.alteredstates.tobacco.craving")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
        }
    }

    @Override
    public void onMilkDrank(ServerPlayer player, SubstanceData data) {
        long now = player.level().getGameTime();
        if (hasWithdrawal(data, now)) {
            // La leche NO quita el mono: se vuelve a aplicar de inmediato
            int severity = 1 + data.dependency / 40;
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, severity - 1, true, false, false));
            if (severity >= 2) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false, false));
            }
            player.displayClientMessage(
                    Component.translatable("message.alteredstates.milk_no_effect_tobacco")
                            .withStyle(ChatFormatting.DARK_RED),
                    true
            );
        }
    }
}

