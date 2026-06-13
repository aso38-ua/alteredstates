package com.alteredstates.compat.fd;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public class CannabutterCookingRecipe extends CookingPotRecipe {

    public CannabutterCookingRecipe(CookingPotRecipe original) {
        // 🍳 CORREGIDO: Cambiamos el Provider.EMPTY por 'null'.
        // Como el resultado de la olla es estático, no necesita escanear los registros del mundo.
        super(
                original.getGroup(),
                original.getRecipeBookTab(),
                original.getIngredients(),
                original.getResultItem(null),
                original.getOutputContainer(),
                original.getExperience(),
                original.getCookTime()
        );
    }

    @Override
    public ItemStack assemble(RecipeWrapper inv, HolderLookup.Provider provider) {
        // Sacamos el resultado base (La mantequilla "vacía")
        ItemStack result = super.assemble(inv, provider).copy();

        int minQuality = Integer.MAX_VALUE;
        boolean hasWeed = false;
        boolean isIndica = true;

        // Escaneamos la olla buscando nuestra hierba
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
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

        // Inyectamos los datos a la mantequilla resultante
        result.set(ModDataComponentTypes.IS_INDICA.get(), isIndica);
        result.set(ModDataComponentTypes.QUALITY.get(), finalQuality);

        return result;
    }
}