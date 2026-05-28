package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, AlteredStates.MOD_ID);

    // ════════════════════════════════════════════════════════════
    //  Efectos custom que no existen en vanilla:
    //   MICRODOSE_EFFECT   — buffs sutiles acumulables de setas
    //   TOLERANCE_EFFECT   — marcador interno (no visible al jugador)
    //   COMEDOWN_EFFECT    — bajón genérico con parámetros
    // ════════════════════════════════════════════════════════════
    // Se registran cuando implementemos el sistema de fases.
}