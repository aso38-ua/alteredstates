package com.alteredstates.item;

public enum CannabisStrain {
    INDICA, SATIVA;

    public static CannabisStrain fromBoolean(boolean isIndica) {
        return isIndica ? INDICA : SATIVA;
    }
}
