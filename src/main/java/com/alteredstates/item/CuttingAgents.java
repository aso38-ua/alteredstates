package com.alteredstates.item;

import com.alteredstates.AlteredStates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Con qué se corta la cocaína. La calidad sale de las tags alteredstates:cutting_agent_q0..q4
 * (otros mods o packs pueden añadir ítems a esas tags).
 * La calidad resultante = min(calidad del aditivo, calidad de la dosis + 2).
 * Los aditivos baratos rinden más cantidad; el mejor aditivo rinde menos.
 */
public final class CuttingAgents {
    private CuttingAgents() {}

    private static final int[] YIELD = {4, 3, 2, 2, 1}; // dosis obtenidas por calidad del aditivo (0..4)

    public static final TagKey<Item>[] TAGS = buildTags();

    @SuppressWarnings("unchecked")
    private static TagKey<Item>[] buildTags() {
        TagKey<Item>[] tags = new TagKey[5];
        for (int q = 0; q < 5; q++) {
            tags[q] = TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "cutting_agent_q" + q));
        }
        return tags;
    }

    /** Calidad del aditivo (0..4) o -1 si no sirve para cortar. */
    public static int qualityOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int q = 4; q >= 0; q--) {
            if (stack.is(TAGS[q])) return q;
        }
        return -1;
    }

    public static int yieldFor(int agentQuality) {
        return YIELD[Math.max(0, Math.min(4, agentQuality))];
    }

    public static int resultQuality(int powderQuality, int agentQuality) {
        return Math.max(0, Math.min(agentQuality, Math.min(4, powderQuality + 2)));
    }
}
