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
        int lowestQuality = 3; // Empezamos en lo más alto
        boolean isIndica = true; // Valor por defecto
        boolean foundButter = false;

        // Buscamos la mantequilla en la mesa
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.CANNABUTTER.get())) {
                foundButter = true;

                // 1. Extraemos la calidad
                int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                if (quality < lowestQuality) {
                    lowestQuality = quality;
                }

                // 2. Extraemos el tipo de marihuana (Asumiendo que tienes un DataComponent para esto)
                // Si lo guardas de otra forma, ajusta esta línea a tu código
                if (stack.has(ModDataComponentTypes.IS_INDICA.get())) {
                    isIndica = stack.getOrDefault(ModDataComponentTypes.IS_INDICA.get(), true);
                }
            }
        }

        // Creamos el resultado
        ItemStack result = new ItemStack(ModItems.BROWNIE.get());
        if (foundButter) {
            // Le inyectamos la calidad heredada y la cepa al Brownie final
            result.set(ModDataComponentTypes.QUALITY.get(), lowestQuality);
            result.set(ModDataComponentTypes.IS_INDICA.get(), isIndica);
        }

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