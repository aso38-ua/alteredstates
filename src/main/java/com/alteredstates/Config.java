package com.alteredstates;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Declaramos las variables primero
    public static final ModConfigSpec.IntValue DRYING_TIME;
    public static final ModConfigSpec.IntValue CURING_TIME;
    public static final ModConfigSpec.DoubleValue BONG_PARANOIA_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue JOINT_DURATION_MULTIPLIER;

    // Las inicializamos en un bloque estático
    static {
        BUILDER.push("Tiempos_de_Procesado");

        DRYING_TIME = BUILDER
                .comment("Tiempo en ticks que tarda en secarse un producto en el secadero (Default: 24000 = 1 dia de juego)")
                .defineInRange("dryingTime", 24000, 100, 240000);

        CURING_TIME = BUILDER
                .comment("Tiempo en ticks que tarda el tarro en subir 1 nivel de calidad (Default: 6000 = 5 minutos reales)")
                .defineInRange("curingTime", 6000, 100, 100000);

        BUILDER.pop();

        BUILDER.push("Balanceo_y_Dificultad");

        BONG_PARANOIA_MULTIPLIER = BUILDER
                .comment("Multiplicador de probabilidad de paranoia al usar el bong (Default: 1.5)")
                .defineInRange("bongParanoiaMultiplier", 1.5, 0.0, 5.0);

        JOINT_DURATION_MULTIPLIER = BUILDER
                .comment("Multiplicador de duracion de los efectos del porro (Default: 1.0)")
                .defineInRange("jointDurationMultiplier", 1.0, 0.1, 5.0);

        BUILDER.pop();
    }

    // Asegúrate de que esto sea public para que AlteredStates.java pueda registrarlo si lo necesitas
    public static final ModConfigSpec SPEC = BUILDER.build();
}