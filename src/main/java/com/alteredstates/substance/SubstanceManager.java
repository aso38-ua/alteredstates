package com.alteredstates.substance;

import com.alteredstates.cocaine.CocaineEffects;
import com.alteredstates.data.attachment.PlayerSubstanceData;
import com.alteredstates.registry.ModAttachments;
import com.alteredstates.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gestor central de sustancias, dependencias, mono y tolerancia.
 * Coordina todas las sustancias registradas y el efecto de la leche / comandos.
 *
 * MONO: mientras algún mono esté activo, el jugador lleva el efecto marcador WITHDRAWAL (duración infinita).
 *  - Leche:           el marcador se vuelve a poner (la leche no cura el mono).
 *  - /effect clear:   el marcador desaparece sin leche -> se suprime el mono hasta la próxima dosis.
 */
public final class SubstanceManager {

    private static final Map<String, ISubstance> SUBSTANCES = new LinkedHashMap<>();
    private static final Map<UUID, Map<String, SubstanceData>> PLAYER_DATA = new HashMap<>();
    private static final Map<UUID, Long> LAST_MILK_DRANK = new HashMap<>();
    private static final Set<UUID> WITHDRAWAL_MARKED = new HashSet<>();
    private static final Set<UUID> WITHDRAWAL_SUPPRESSED = new HashSet<>();

    static {
        registerSubstance(new TobaccoSubstance());
        registerSubstance(new CannabisSubstance());
    }

    private SubstanceManager() {}

    public static void registerSubstance(ISubstance substance) {
        SUBSTANCES.put(substance.getId(), substance);
    }

    public static ISubstance getSubstance(String id) {
        return SUBSTANCES.get(id);
    }

    public static Collection<ISubstance> all() {
        return SUBSTANCES.values();
    }

    public static SubstanceData getOrCreateData(ServerPlayer player, String substanceId) {
        UUID uuid = player.getUUID();
        Map<String, SubstanceData> map = PLAYER_DATA.computeIfAbsent(uuid, k -> new HashMap<>());
        return map.computeIfAbsent(substanceId, k -> {
            // Inicializar desde PlayerSubstanceData si existe
            PlayerSubstanceData att = player.getData(ModAttachments.SUBSTANCE_DATA);
            SubstanceData d = new SubstanceData();
            d.tolerance = att.getTolerance(substanceId);
            d.dependency = att.getDependency(substanceId);
            d.lastUse = att.getLastConsumption(substanceId);
            return d;
        });
    }

    public static void onConsume(ServerPlayer player, String substanceId, ItemStack stack, int quality) {
        ISubstance substance = SUBSTANCES.get(substanceId);
        if (substance != null) {
            clearWithdrawalSuppression(player);
            substance.onConsume(player, stack, quality);
            syncAttachment(player, substanceId);
        }
    }

    public static void onMilkDrank(ServerPlayer player) {
        long now = player.level().getGameTime();
        LAST_MILK_DRANK.put(player.getUUID(), now);

        // Notificar a todas las sustancias activas
        for (ISubstance substance : SUBSTANCES.values()) {
            SubstanceData data = getOrCreateData(player, substance.getId());
            substance.onMilkDrank(player, data);
        }

        // Para cocaína: CocaineEffects gestiona su propia respuesta a la leche
        CocaineEffects.onMilkDrank(player);
    }

    /** Comprueba si el jugador bebió leche hace menos de 15 ticks */
    public static boolean didDrinkMilkRecently(ServerPlayer player) {
        Long time = LAST_MILK_DRANK.get(player.getUUID());
        if (time == null) return false;
        long diff = player.level().getGameTime() - time;
        return diff >= 0 && diff <= 15;
    }

    // ───────────────── Supresión del mono (/effect clear) ─────────────────

    public static boolean isWithdrawalSuppressed(ServerPlayer player) {
        return WITHDRAWAL_SUPPRESSED.contains(player.getUUID());
    }

    /** Se llama al consumir cualquier sustancia: el mono vuelve a poder aparecer más adelante. */
    public static void clearWithdrawalSuppression(ServerPlayer player) {
        WITHDRAWAL_SUPPRESSED.remove(player.getUUID());
    }

    private static void updateWithdrawalMarker(ServerPlayer player, long now) {
        UUID id = player.getUUID();

        boolean any = CocaineEffects.hasWithdrawal(player, now);
        if (!any) {
            for (ISubstance s : SUBSTANCES.values()) {
                if (s.hasWithdrawal(getOrCreateData(player, s.getId()), now)) { any = true; break; }
            }
        }

        boolean has = player.hasEffect(ModEffects.WITHDRAWAL);

        if (!any) {
            if (has) player.removeEffect(ModEffects.WITHDRAWAL);
            WITHDRAWAL_MARKED.remove(id);
            return;
        }
        if (WITHDRAWAL_SUPPRESSED.contains(id)) {
            if (has) player.removeEffect(ModEffects.WITHDRAWAL);
            return;
        }
        if (has) {
            WITHDRAWAL_MARKED.add(id);
            return;
        }
        // Había marcador y ya no está
        if (WITHDRAWAL_MARKED.contains(id) && !didDrinkMilkRecently(player)) {
            WITHDRAWAL_SUPPRESSED.add(id);
            WITHDRAWAL_MARKED.remove(id);
            player.displayClientMessage(Component.translatable("message.alteredstates.withdrawal.cleared")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        // Primera vez, o la leche lo quitó: se vuelve a poner
        player.addEffect(new MobEffectInstance(ModEffects.WITHDRAWAL, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
        WITHDRAWAL_MARKED.add(id);
    }

    // ───────────────── Tick / sincronización ─────────────────

    public static void tick(ServerPlayer player) {
        long now = player.level().getGameTime();
        for (ISubstance substance : SUBSTANCES.values()) {
            SubstanceData data = getOrCreateData(player, substance.getId());
            substance.tick(player, data);
        }
        updateWithdrawalMarker(player, now);
        if (now % 1200 == 0) syncAll(player);
    }

    private static void syncAttachment(ServerPlayer player, String substanceId) {
        SubstanceData d = getOrCreateData(player, substanceId);
        PlayerSubstanceData att = player.getData(ModAttachments.SUBSTANCE_DATA);
        att.setTolerance(substanceId, d.tolerance);
        att.setDependency(substanceId, d.dependency);
        att.setLastConsumption(substanceId, d.lastUse);
    }

    public static void syncAll(ServerPlayer player) {
        for (String id : SUBSTANCES.keySet()) syncAttachment(player, id);
    }

    /** Al salir del mundo: guarda el estado y libera la caché (si no, al cambiar de mundo se mezclan datos). */
    public static void onLogout(ServerPlayer player) {
        syncAll(player);
        UUID id = player.getUUID();
        PLAYER_DATA.remove(id);
        LAST_MILK_DRANK.remove(id);
        WITHDRAWAL_MARKED.remove(id);
        WITHDRAWAL_SUPPRESSED.remove(id);
    }

    // ───────────────── Reset (comando) ─────────────────

    public static void reset(ServerPlayer player, String substanceId) {
        Map<String, SubstanceData> map = PLAYER_DATA.get(player.getUUID());
        if (map != null) map.remove(substanceId);
        player.getData(ModAttachments.SUBSTANCE_DATA).resetSubstance(substanceId);
    }

    public static void resetAll(ServerPlayer player) {
        for (String id : SUBSTANCES.keySet()) reset(player, id);
        CocaineEffects.reset(player);
        WITHDRAWAL_SUPPRESSED.remove(player.getUUID());
        player.removeEffect(ModEffects.WITHDRAWAL);
        WITHDRAWAL_MARKED.remove(player.getUUID());
    }
}
