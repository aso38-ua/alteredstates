package com.alteredstates.event;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AlteredStates.MOD_ID)
public class RollingTrayInteractionHandler {

    /**
     * Intercepta el clic derecho sobre la bandeja de liar (Rolling Tray).
     * Con mods como Carry On instalados, hacer Shift + Clic derecho con la mano vacía
     * normalmente intentaría coger la bandeja como entidad transportable en lugar de liar.
     * Al procesar la interacción con prioridad HIGHEST, ejecutamos directamente el liado
     * y cancelamos el evento para que Carry On u otros mods no puedan coger la bandeja.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(ModBlocks.ROLLING_TRAY.get())) {
            Player player = event.getEntity();
            // Si el jugador está agachado (Shift) e interactúa con la mano vacía (acción de liar)
            if (player.isShiftKeyDown() && event.getItemStack().isEmpty()) {
                InteractionResult result = state.useWithoutItem(level, player, event.getHitVec());
                if (result.consumesAction()) {
                    event.setCancellationResult(result);
                    event.setCanceled(true);
                }
            }
        }
    }
}
