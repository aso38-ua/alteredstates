package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.recipe.BrownieRecipe;
import com.alteredstates.recipe.CannabutterRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    // 📖 Creamos el registro diferido para los Serializadores de Recetas
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AlteredStates.MOD_ID);

    // 🧈 Registramos el serializador especial para la Mantequilla Canábica
    public static final Supplier<RecipeSerializer<CannabutterRecipe>> CANNABUTTER_SERIALIZER =
            SERIALIZERS.register("crafting_special_cannabutter",
                    () -> new SimpleCraftingRecipeSerializer<>(CannabutterRecipe::new));

    public static final Supplier<RecipeSerializer<BrownieRecipe>> BROWNIE_SERIALIZER =
            SERIALIZERS.register("crafting_special_brownie",
                    () -> new SimpleCraftingRecipeSerializer<>(BrownieRecipe::new));
}