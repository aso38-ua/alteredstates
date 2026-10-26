package com.alteredstates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Locale;

/** Calor del bloque que hay DEBAJO de una posición: 0 frío, 1 normal, 2 potenciado (Blaze Burner). */
public final class HeatHelper {
    private HeatHelper() {}

    public static int getHeatBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(BlockTags.CAMPFIRES)) {
            return below.hasProperty(CampfireBlock.LIT) && below.getValue(CampfireBlock.LIT) ? 1 : 0;
        }
        if (below.is(BlockTags.FIRE) || below.getFluidState().is(FluidTags.LAVA)) return 1;

        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(below.getBlock());
        if (key.getNamespace().equals("create") && key.getPath().equals("blaze_burner")) {
            for (Property<?> p : below.getProperties()) {
                if (p.getName().equals("blaze")) {
                    String v = valueName(below, p);
                    if (v.equals("none")) return 0;
                    if (v.equals("smouldering")) return 1;
                    return 2;
                }
            }
        }
        return 0;
    }

    private static <T extends Comparable<T>> String valueName(BlockState s, Property<T> p) {
        return p.getName(s.getValue(p)).toLowerCase(Locale.ROOT);
    }
}
