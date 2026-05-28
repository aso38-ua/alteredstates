package com.alteredstates.compat;

/**
 * Recetas y hooks de Farmer's Delight.
 * Este método solo se llama si CompatManager.FARMERS_DELIGHT == true.
 * La clase no se carga si FD no está → sin ClassNotFoundException.
 */
public class FarmersDelightCompat {

    public static void init() {
        // Aquí irán:
        //  - Recetas de CookingPot (chocolate caliente, fondue, risotto...)
        //  - Registro del CuttingBoard recipe type para cogollos
        //  - Comfort values para comestibles del mod
    }
}