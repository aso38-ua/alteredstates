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
        // 🔥 CORRECCIÓN: Inicializamos en 4 (Premium) para que no cape las calidades superiores
        int lowestQuality = com.alteredstates.item.CannabisQuality.PREMIUM.getLevel();
        boolean isIndica = true;
        boolean foundButter = false;

        // Buscamos la mantequilla en la mesa
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.CANNABUTTER.get())) {
                foundButter = true;

                // Extraemos la calidad real de la mantequilla
                int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                // Al ser un solo ingrediente cannábico, esto asignará el valor real (ej: 4)
                // Si en el futuro añades más ingredientes, mantendrá el peor de ellos.
                if (quality < lowestQuality) {
                    lowestQuality = quality;
                } else if (inv.items().stream().filter(s -> s.is(ModItems.CANNABUTTER.get())).count() == 1) {
                    // Si solo hay una mantequilla, nos aseguramos de que asigne su calidad exacta directamente
                    lowestQuality = quality;
                }

                // Extraemos el tipo de marihuana
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