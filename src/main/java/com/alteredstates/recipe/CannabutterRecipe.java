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
        int milkCount = 0;
        int bowlCount = 0;
        int indicaCount = 0;
        int sativaCount = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(net.minecraft.world.item.Items.MILK_BUCKET)) milkCount++;
            else if (stack.is(net.minecraft.world.item.Items.BOWL)) bowlCount++;
            else if (stack.is(com.alteredstates.registry.ModItems.INDICA_GROUND.get())) indicaCount++;
            else if (stack.is(com.alteredstates.registry.ModItems.SATIVA_GROUND.get())) sativaCount++;
            else return false; // Si hay cualquier otro ítem, la receta falla
        }

        // Acepta la receta SI hay 1 leche, 1 bol, y (3 índicas O 3 sativas)
        return milkCount == 1 && bowlCount == 1 && ((indicaCount == 3 && sativaCount == 0) || (sativaCount == 3 && indicaCount == 0));
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        int lowestQuality = 3;
        boolean isIndica = true;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.is(com.alteredstates.registry.ModItems.INDICA_GROUND.get()) || stack.is(com.alteredstates.registry.ModItems.SATIVA_GROUND.get())) {

                // Si detecta Sativa, marcamos el boolean como falso
                if (stack.is(com.alteredstates.registry.ModItems.SATIVA_GROUND.get())) {
                    isIndica = false;
                }

                // Hereda la peor calidad
                int quality = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), 1);
                if (quality < lowestQuality) {
                    lowestQuality = quality;
                }
            }
        }

        ItemStack result = new ItemStack(com.alteredstates.registry.ModItems.CANNABUTTER.get());
        result.set(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), lowestQuality);

        // Le guardamos el tipo de cepa a la mantequilla
        result.set(com.alteredstates.registry.ModDataComponentTypes.IS_INDICA.get(), isIndica);

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