package com.alteredstates.compat.fd;

import com.alteredstates.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeSerializers;
import java.util.function.Supplier;

public class FDRecipes {

    public static final Supplier<RecipeSerializer<?>> CANNABUTTER_COOKING_SERIALIZER =
            ModRecipes.SERIALIZERS.register("cooking_special_cannabutter",
                    () -> new RecipeSerializer<CannabutterCookingRecipe>() {

                        // 🪄 MAGIA NEGRA: Engañamos al compilador con (Object) para saltarnos el límite de los genéricos
                        @SuppressWarnings("unchecked")
                        private final MapCodec<CannabutterCookingRecipe> codec =
                                ((MapCodec<CookingPotRecipe>) (Object) ModRecipeSerializers.COOKING.get().codec())
                                        .xmap(CannabutterCookingRecipe::new, recipe -> recipe);

                        @SuppressWarnings("unchecked")
                        private final StreamCodec<RegistryFriendlyByteBuf, CannabutterCookingRecipe> streamCodec =
                                ((StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe>) (Object) ModRecipeSerializers.COOKING.get().streamCodec())
                                        .map(CannabutterCookingRecipe::new, recipe -> recipe);

                        @Override public MapCodec<CannabutterCookingRecipe> codec() { return codec; }
                        @Override public StreamCodec<RegistryFriendlyByteBuf, CannabutterCookingRecipe> streamCodec() { return streamCodec; }
                    });

    public static void register() {}
}