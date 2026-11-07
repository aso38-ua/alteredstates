package com.alteredstates.substance;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/**
 * Lógica de tolerancia y leve abstinencia para el cannabis (porros, bong, comestibles).
 * Genera tolerancia (dosis duran menos con el uso frecuente) y dependencia más leve
 * que cocaína o tabaco (desánimo / apatía / falta de apetito o cansancio).
 */
public class CannabisSubstance implements ISubstance {

    public static final String ID = "cannabis";
    private static final int TOLERANCE_RESET = 96_000;    // 4 días de juego
    private static final int WITHDRAWAL_AFTER = 24_000;   // 1 día entero sin consumir
    private static final int WITHDRAWAL_MIN_DEP = 35;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("substance.alteredstates.cannabis");
    }

    @Override
    public Component getWarningTooltip() {
        return Component.translatable("tooltip.alteredstates.cannabis.warning")
                .withStyle(ChatFormatting.DARK_GREEN);
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
        data.dependency = Math.min(100, data.dependency + 5 + quality);
        data.lastUse = now;

        if (wasInWithdrawal) {
            player.removeEffect(MobEffects.WEAKNESS);
            player.displayClientMessage(
                    Component.translatable("message.alteredstates.cannabis.calmed")
                            .withStyle(ChatFormatting.GREEN),
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

        if (data.dependency > 0 && now - data.lastUse > 24_000 && now % 6000 == 0) {
            data.dependency--;
        }

        if (hasWithdrawal(data, now) && !SubstanceManager.isWithdrawalSuppressed(player)) {
            if (now % 40 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
            }

            if (now % 2800 == 0) {
                player.displayClientMessage(
                        Component.translatable("message.alteredstates.cannabis.craving")
                                .withStyle(ChatFormatting.GRAY),
                        true
                );
            }
        }
    }

    @Override
    public void onMilkDrank(ServerPlayer player, SubstanceData data) {
        long now = player.level().getGameTime();
        if (hasWithdrawal(data, now) && !SubstanceManager.isWithdrawalSuppressed(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
            player.displayClientMessage(
                    Component.translatable("message.alteredstates.milk_no_effect_cannabis")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    true
            );
        }
    }
}

