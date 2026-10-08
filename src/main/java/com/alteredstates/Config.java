package com.alteredstates;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Declaramos las variables de configuración
    public static final ModConfigSpec.IntValue DRYING_TIME;
    public static final ModConfigSpec.IntValue CURING_TIME;
    public static final ModConfigSpec.DoubleValue BONG_PARANOIA_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue JOINT_DURATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue EDIBLE_DURATION_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ALLOW_PARANOIA;
    public static final ModConfigSpec.BooleanValue ENABLE_SCREEN_SHADERS;

    static {
        BUILDER.push("Tiempos_de_Procesado");

        DRYING_TIME = BUILDER
                .comment("Tiempo base en ticks que tarda en secarse un producto en el secadero (Default: 24000 = 1 dia de juego)")
                .defineInRange("dryingTime", 24000, 100, 240000);

        CURING_TIME = BUILDER
                .comment("Tiempo base en ticks que tarda el tarro en subir 1 nivel de calidad (Default: 6000 = 5 minutos reales)")
                .defineInRange("curingTime", 6000, 100, 100000);

        BUILDER.pop();

        BUILDER.push("Balanceo_y_Dificultad");

        BONG_PARANOIA_MULTIPLIER = BUILDER
                .comment("Multiplicador de probabilidad de paranoia al usar el bong (Default: 1.5)")
                .defineInRange("bongParanoiaMultiplier", 1.5, 0.0, 5.0);

        JOINT_DURATION_MULTIPLIER = BUILDER
                .comment("Multiplicador de duracion de los efectos de los porros y cigarrillos (Default: 1.0)")
                .defineInRange("jointDurationMultiplier", 1.0, 0.1, 5.0);

        EDIBLE_DURATION_MULTIPLIER = BUILDER
                .comment("Multiplicador de duracion de los efectos de comestibles (Brownies, Mantequilla) (Default: 1.0)")
                .defineInRange("edibleDurationMultiplier", 1.0, 0.1, 5.0);

        ALLOW_PARANOIA = BUILDER
                .comment("Permite o deshabilita el efecto de Paranoia (Mal Viaje) en el servidor (Default: true)")
                .define("allowParanoia", true);

        ENABLE_SCREEN_SHADERS = BUILDER
                .comment("Habilita o deshabilita los shaders visuales de viaje/humo en pantalla (Default: true)")
                .define("enableScreenShaders", true);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}