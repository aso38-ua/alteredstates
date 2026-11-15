package com.alteredstates.item;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Nombres de los cuatro clips de primera persona DENTRO de un archivo .animation.json, y las
 * RawAnimation ya construidas (se construyen una vez; GeckoLib compara la animación actual con ellas).
 *
 *   idle        bucle en reposo
 *   raise+hold  al empezar a fumar: raise una vez y luego hold en bucle
 *   lower+idle  al soltar: lower una vez y luego idle en bucle
 *
 * DEFAULT usa los nombres "idle", "raise", "hold", "lower" (los de cigarette_fp.animation.json).
 * Si un mismo archivo tuviera varias animaciones, se crean otros nombres: new FpClips("idle","cigar_raise",...).
 */
public final class FpClips {
    public static final FpClips DEFAULT = new FpClips("idle", "raise", "hold", "lower");

    private final RawAnimation idle;
    private final RawAnimation raiseHold;
    private final RawAnimation lowerIdle;

    public FpClips(String idle, String raise, String hold, String lower) {
        this.idle = RawAnimation.begin().thenLoop(idle);
        this.raiseHold = RawAnimation.begin().thenPlay(raise).thenLoop(hold);
        this.lowerIdle = RawAnimation.begin().thenPlay(lower).thenLoop(idle);
    }

    public RawAnimation idle() { return idle; }
    public RawAnimation raiseHold() { return raiseHold; }
    public RawAnimation lowerIdle() { return lowerIdle; }
}
