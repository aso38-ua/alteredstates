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

public class BrownieRecipe extends CustomRecipe {

    public BrownieRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int butterCount = 0;
        int wheatCount = 0;
        int cocoaCount = 0;

        for (ItemStack stack : inv.items()) {
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.CANNABUTTER.get())) butterCount++;
                else if (stack.is(Items.WHEAT)) wheatCount++;
                else if (stack.is(Items.COCOA_BEANS)) cocoaCount++;
                else return false;
            }
        }
        return butterCount == 1 && wheatCount == 1 && cocoaCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        boolean isIndica = true;
        int quality = 1;

        // Buscamos la mantequilla usada para clonar sus propiedades
        for (ItemStack stack : inv.items()) {
            if (!stack.isEmpty() && stack.is(ModItems.CANNABUTTER.get())) {
                isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);
                quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                break;
            }
        }

        // Creamos el brownie resultante con los datos heredados
        ItemStack result = new ItemStack(ModItems.BROWNIE.get()); // Asegúrate de usar el ID de tu brownie
        result.set(ModDataComponentTypes.IS_INDICA.get(), isIndica);
        result.set(ModDataComponentTypes.QUALITY.get(), quality);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return com.alteredstates.registry.ModRecipes.BROWNIE_SERIALIZER.get();
    }
}