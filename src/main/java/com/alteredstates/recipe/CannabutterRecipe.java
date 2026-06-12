package com.alteredstates.recipe;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

public class CannabutterRecipe extends CustomRecipe {

    public CannabutterRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int weedCount = 0;
        int milkCount = 0;
        int bowlCount = 0;
        boolean hasIndica = false;
        boolean hasSativa = false;

        // 🔄 CORREGIDO: Usamos inv.items() para iterar de forma segura en 1.21
        for (ItemStack stack : inv.items()) {
            if (!stack.isEmpty()) {
                if (stack.is(Items.MILK_BUCKET)) {
                    milkCount++;
                } else if (stack.is(Items.BOWL)) {
                    bowlCount++;
                } else if (stack.is(ModItems.INDICA_GROUND.get())) {
                    weedCount++;
                    hasIndica = true;
                } else if (stack.is(ModItems.SATIVA_GROUND.get())) {
                    weedCount++;
                    hasSativa = true;
                } else {
                    return false;
                }
            }
        }

        return milkCount == 1 && bowlCount == 1 && weedCount == 3 && !(hasIndica && hasSativa);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        int minQuality = Integer.MAX_VALUE;
        boolean hasWeed = false;
        boolean isIndica = true;

        // 🔄 CORREGIDO: Iteración limpia sobre inv.items()
        for (ItemStack stack : inv.items()) {
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.SATIVA_GROUND.get())) {
                    isIndica = false;
                }
                if (stack.is(ModItems.INDICA_GROUND.get()) || stack.is(ModItems.SATIVA_GROUND.get())) {
                    int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                    if (quality < minQuality) {
                        minQuality = quality;
                    }
                    hasWeed = true;
                }
            }
        }

        int finalQuality = hasWeed ? minQuality : 1;

        ItemStack result = new ItemStack(ModItems.CANNABUTTER.get());
        result.set(ModDataComponentTypes.IS_INDICA.get(), isIndica);
        result.set(ModDataComponentTypes.QUALITY.get(), finalQuality);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 5;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return com.alteredstates.registry.ModRecipes.CANNABUTTER_SERIALIZER.get();
    }
}