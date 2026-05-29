package com.alteredstates.item;

import net.minecraft.util.StringRepresentable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum CannabisQuality implements StringRepresentable {
    BASURA(0, "basura", "quality.alteredstates.basura", ChatFormatting.GRAY),
    REGULAR(1, "regular", "quality.alteredstates.regular", ChatFormatting.WHITE),
    NORMAL(2, "normal", "quality.alteredstates.normal", ChatFormatting.AQUA),
    BUENA(3, "buena", "quality.alteredstates.buena", ChatFormatting.GREEN),
    PREMIUM(4, "premium", "quality.alteredstates.premium", ChatFormatting.GOLD);

    private final int level;
    private final String name;
    private final String translationKey;
    private final ChatFormatting format;

    CannabisQuality(int level, String name, String translationKey, ChatFormatting format) {
        this.level = level;
        this.name = name;
        this.translationKey = translationKey;
        this.format = format;
    }

    public int getLevel() { return level; }
    public ChatFormatting getFormat() { return format; }

    public Component getTranslatedName() {
        return Component.translatable(this.translationKey).withStyle(this.format);
    }

    @Override
    public String getSerializedName() { return this.name; }

    public static CannabisQuality byLevel(int level) {
        if (level <= 0) return BASURA;
        if (level == 1) return REGULAR;
        if (level == 2) return NORMAL;
        if (level == 3) return BUENA;
        return PREMIUM;
    }
}