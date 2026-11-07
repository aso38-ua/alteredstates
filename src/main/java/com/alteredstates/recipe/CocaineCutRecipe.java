package com.alteredstates.recipe;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import com.alteredstates.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Cortar la cocaína: 1 polvo + 1 aditivo de corte = 2 polvos con UN nivel menos de calidad.
 * Más cantidad, pero pega menos (y como el efecto depende de la calidad, es un trueque real).
 * No se puede cortar la calidad 0.
 */
public class CocaineCutRecipe extends CustomRecipe {

    public CocaineCutRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static int quality(ItemStack s) {
        return s.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int powder = 0, agent = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ModItems.COCAINE_POWDER.get())) {
                if (quality(s) < 1) return false;
                powder++;
            } else if (s.is(ModItems.CUTTING_AGENT.get())) {
                agent++;
            } else {
                return false;
            }
        }
        return powder == 1 && agent == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int q = 2;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.is(ModItems.COCAINE_POWDER.get())) q = quality(s);
        }
        ItemStack out = new ItemStack(ModItems.COCAINE_POWDER.get(), 2);
        out.set(ModDataComponentTypes.QUALITY.get(), Math.max(0, q - 1));
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COCAINE_CUT.get();
    }
}
