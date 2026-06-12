package com.alteredstates.compat;

import com.alteredstates.compat.fd.FDEvents;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Recetas y hooks de Farmer's Delight.
 * Este método solo se llama si CompatManager.FARMERS_DELIGHT == true.
 * La clase no se carga si FD no está → sin ClassNotFoundException.
 */
public class FarmersDelightCompat {

    public static void init() {
        // Registramos dinámicamente los eventos de Farmer's Delight
        NeoForge.EVENT_BUS.register(FDEvents.class);
    }
}