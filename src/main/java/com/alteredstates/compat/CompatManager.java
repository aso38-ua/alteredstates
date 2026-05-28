package com.alteredstates.compat;

import net.neoforged.fml.ModList;

/**
 * Punto central de detección de mods opcionales.
 * Todos los boolean son finales → el JIT los trata como constantes.
 * Nunca crashea si el mod no está presente.
 */
public final class CompatManager {

    public static final boolean FARMERS_DELIGHT  = ModList.get().isLoaded("farmersdelight");
    public static final boolean CREATE            = ModList.get().isLoaded("create");
    public static final boolean SERENE_SEASONS    = ModList.get().isLoaded("sereneseasons");
    public static final boolean JEI               = ModList.get().isLoaded("jei");
    public static final boolean EMI               = ModList.get().isLoaded("emi");
    public static final boolean BIOMES_O_PLENTY   = ModList.get().isLoaded("biomesoplenty");
    public static final boolean OH_THE_BIOMES     = ModList.get().isLoaded("byg");

    private CompatManager() {}
}