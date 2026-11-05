package com.alteredstates.item;

import com.alteredstates.AlteredStates;
import net.minecraft.resources.ResourceLocation;

public enum SmokeAnimation {
    //          base de los clips   raise  lower  (ticks; duración real del clip en segundos × 20)
    SMOKING    ("smoking",          7,     7),   // por defecto; sin clips => sin animación
    CIGARETTE  ("cigarette",        7,     7);
    // PIPE("pipe", 10, 8), JOINT("joint", ...) ...

    private final ResourceLocation id;
    private final int raiseTicks;
    private final int lowerTicks;

    SmokeAnimation(String name, int raiseTicks, int lowerTicks) {
        this.id = ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, name);
        this.raiseTicks = raiseTicks;
        this.lowerTicks = lowerTicks;
    }

    public ResourceLocation id() { return id; }
    public int raiseTicks() { return raiseTicks; }
    public int lowerTicks() { return lowerTicks; }
}