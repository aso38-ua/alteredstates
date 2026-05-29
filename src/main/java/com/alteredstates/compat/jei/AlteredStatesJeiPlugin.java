package com.alteredstates.compat.jei;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AlteredStatesJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_plugin");

    public static final RecipeType<DryingRecipeWrapper> DRYING_TYPE =
            RecipeType.create(AlteredStates.MOD_ID, "drying", DryingRecipeWrapper.class);
    public static final RecipeType<CuringRecipeWrapper> CURING_TYPE =
            RecipeType.create(AlteredStates.MOD_ID, "curing", CuringRecipeWrapper.class);
    // 1. Añadimos el tipo para el Grinder
    public static final RecipeType<GrindingRecipeWrapper> GRINDING_TYPE =
            RecipeType.create(AlteredStates.MOD_ID, "grinding", GrindingRecipeWrapper.class);

    private IDrawable background; // El salvavidas para que EMI no crashee
    private IDrawable iconDrying;
    private IDrawable iconCuring;
    private IDrawable iconGrinder;

    @Override
    public ResourceLocation getPluginUid() { return PLUGIN_ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        // 🛠️ FIX PARA EMI: Creamos un fondo invisible de 140x35
        this.background = guiHelper.createBlankDrawable(140, 35);

        this.iconDrying = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DRYING_RACK.get()));
        this.iconCuring = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CURING_JAR.get()));
        this.iconGrinder = guiHelper.createDrawableItemStack(new ItemStack(ModItems.GRINDER.get()));

        // 🟢 Categoría del Secadero
        registration.addRecipeCategories(new IRecipeCategory<DryingRecipeWrapper>() {
            @Override public RecipeType<DryingRecipeWrapper> getRecipeType() { return DRYING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.drying"); }
            @Override public IDrawable getBackground() { return background; } // <-- OBLIGATORIO PARA EMI
            @Override public IDrawable getIcon() { return iconDrying; }

            @Override
            public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }

            @Override
            public void draw(DryingRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
                guiGraphics.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });

        // 🟢 Categoría del Tarro de Curado
        registration.addRecipeCategories(new IRecipeCategory<CuringRecipeWrapper>() {
            @Override public RecipeType<CuringRecipeWrapper> getRecipeType() { return CURING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.curing"); }
            @Override public IDrawable getBackground() { return background; } // <-- OBLIGATORIO PARA EMI
            @Override public IDrawable getIcon() { return iconCuring; }

            @Override
            public void setRecipe(IRecipeLayoutBuilder builder, CuringRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 4).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 4).addItemStack(recipe.output());
            }

            @Override
            public void draw(CuringRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
                Component infoText = Component.translatable("jei.alteredstates.improves_quality");
                guiGraphics.drawString(Minecraft.getInstance().font, infoText, 24, 24, 0xFF44AA44, false);
                guiGraphics.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 8, 0xFF555555, false);
            }
        });

        // 🟢 Categoría del Grinder
        registration.addRecipeCategories(new IRecipeCategory<GrindingRecipeWrapper>() {
            @Override public RecipeType<GrindingRecipeWrapper> getRecipeType() { return GRINDING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.grinding"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconGrinder; }

            @Override
            public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }

            @Override
            public void draw(GrindingRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
                guiGraphics.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 🟢 RECETAS DE SECADO (Drying)
        List<DryingRecipeWrapper> dryingRecipes = new ArrayList<>();

        // Indica
        ItemStack indicaGreen = new ItemStack(ModItems.INDICA_BUDS_FRESH.get());
        ItemStack indicaDry = new ItemStack(ModItems.INDICA_BUDS_DRY.get());
        indicaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(indicaGreen, indicaDry));

        // Sativa
        ItemStack sativaGreen = new ItemStack(ModItems.SATIVA_BUDS_FRESH.get());
        ItemStack sativaDry = new ItemStack(ModItems.SATIVA_BUDS_DRY.get());
        sativaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(sativaGreen, sativaDry));

        registration.addRecipes(DRYING_TYPE, dryingRecipes);


        // 🟢 RECETAS DE CURADO (Curing)
        List<CuringRecipeWrapper> curingRecipes = new ArrayList<>();

        // Indica
        ItemStack indicaInCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get());
        indicaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack indicaOutCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get());
        indicaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(indicaInCure, indicaOutCure));

        // Sativa
        ItemStack sativaInCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get());
        sativaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack sativaOutCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get());
        sativaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(sativaInCure, sativaOutCure));

        registration.addRecipes(CURING_TYPE, curingRecipes);


        // 🟢 RECETAS DE GRINDER (Grinding)
        List<GrindingRecipeWrapper> grindingRecipes = new ArrayList<>();

        // Indica
        ItemStack indicaInGrind = new ItemStack(ModItems.INDICA_BUDS_DRY.get());
        ItemStack indicaOutGrind = new ItemStack(ModItems.INDICA_GROUND.get());
        indicaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(indicaInGrind, indicaOutGrind));

        // Sativa
        ItemStack sativaInGrind = new ItemStack(ModItems.SATIVA_BUDS_DRY.get());
        ItemStack sativaOutGrind = new ItemStack(ModItems.SATIVA_GROUND.get());
        sativaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(sativaInGrind, sativaOutGrind));

        registration.addRecipes(GRINDING_TYPE, grindingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRYING_RACK.get()), DRYING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CURING_JAR.get()), CURING_TYPE);
        // Si le dan a la "U" o "R" sobre el Grinder, saldrá esta receta
        registration.addRecipeCatalyst(new ItemStack(ModItems.GRINDER.get()), GRINDING_TYPE);
    }

    public record DryingRecipeWrapper(ItemStack input, ItemStack output) {}
    public record CuringRecipeWrapper(ItemStack input, ItemStack output) {}
    public record GrindingRecipeWrapper(ItemStack input, ItemStack output) {}
}