package com.alteredstates.item;

import net.minecraft.resources.ResourceLocation;

/**
 * Animación de tercera persona (PAL) de un ítem fumable.
 *
 * Antes era un enum cerrado: para añadir una animación había que editar el enum. Ahora es un record:
 * se crea una con SmokeAnimation.of("cigar", 9, 9) y listo, sin tocar PalSetup.
 *
 * "name" es el prefijo de los tres clips de player_animations/:
 *     <name>_raise.json   <name>_hold.json   <name>_lower.json
 * raiseTicks/lowerTicks = duración en ticks del raise y del lower (PalSetup los usa para que el
 * lower arranque desde el punto justo si sueltas el ítem a medio levantar).
 */
public record SmokeAnimation(ResourceLocation id, int raiseTicks, int lowerTicks) {

    /** Valor por defecto de ISmokableItem. Ojo: no existen smoking_raise/hold/lower.json todavía. */
    public static final SmokeAnimation SMOKING = of("smoking", 7, 7);
    public static final SmokeAnimation CIGARETTE = of("cigarette", 7, 7);

    public static SmokeAnimation of(String name, int raiseTicks, int lowerTicks) {
        return new SmokeAnimation(ResourceLocation.fromNamespaceAndPath("alteredstates", name), raiseTicks, lowerTicks);
    }
}
