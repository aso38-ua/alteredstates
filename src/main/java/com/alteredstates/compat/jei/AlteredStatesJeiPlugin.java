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
import net.minecraft.world.item.Items;
import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AlteredStatesJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_plugin");

    public static final RecipeType<DryingRecipeWrapper> DRYING_TYPE = RecipeType.create(AlteredStates.MOD_ID, "drying", DryingRecipeWrapper.class);
    public static final RecipeType<CuringRecipeWrapper> CURING_TYPE = RecipeType.create(AlteredStates.MOD_ID, "curing", CuringRecipeWrapper.class);
    public static final RecipeType<GrindingRecipeWrapper> GRINDING_TYPE = RecipeType.create(AlteredStates.MOD_ID, "grinding", GrindingRecipeWrapper.class);
    public static final RecipeType<RollingRecipeWrapper> ROLLING_TYPE = RecipeType.create(AlteredStates.MOD_ID, "rolling", RollingRecipeWrapper.class);
    public static final RecipeType<BongRecipeWrapper> BONG_TYPE = RecipeType.create(AlteredStates.MOD_ID, "bong_use", BongRecipeWrapper.class);
    private IDrawable iconBong;

    private IDrawable background;
    private IDrawable iconDrying;
    private IDrawable iconCuring;
    private IDrawable iconGrinder;
    private IDrawable iconRollingTray;

    @Override public ResourceLocation getPluginUid() { return PLUGIN_ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        this.background = guiHelper.createBlankDrawable(140, 35);

        this.iconDrying = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DRYING_RACK.get()));
        this.iconCuring = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CURING_JAR.get()));
        this.iconGrinder = guiHelper.createDrawableItemStack(new ItemStack(ModItems.GRINDER.get()));
        this.iconRollingTray = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ROLLING_TRAY.get()));

        // 🟢 CORRECCIÓN: Añadido <DryingRecipeWrapper> al instanciar la categoría
        registration.addRecipeCategories(new IRecipeCategory<DryingRecipeWrapper>() {
            @Override public RecipeType<DryingRecipeWrapper> getRecipeType() { return DRYING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.drying"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconDrying; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }
            @Override public void draw(DryingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });

        // 🟢 CORRECCIÓN: Añadido <CuringRecipeWrapper> al instanciar la categoría
        registration.addRecipeCategories(new IRecipeCategory<CuringRecipeWrapper>() {
            @Override public RecipeType<CuringRecipeWrapper> getRecipeType() { return CURING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.curing"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconCuring; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, CuringRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 4).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 4).addItemStack(recipe.output());
            }
            @Override public void draw(CuringRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, Component.translatable("jei.alteredstates.improves_quality"), 24, 24, 0xFF44AA44, false);
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 8, 0xFF555555, false);
            }
        });

        // 🟢 CORRECCIÓN: Añadido <GrindingRecipeWrapper> al instanciar la categoría
        registration.addRecipeCategories(new IRecipeCategory<GrindingRecipeWrapper>() {
            @Override public RecipeType<GrindingRecipeWrapper> getRecipeType() { return GRINDING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.grinding"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconGrinder; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }
            @Override public void draw(GrindingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });

        // 🟢 CORRECCIÓN: Añadido <RollingRecipeWrapper> al instanciar la categoría
        registration.addRecipeCategories(new IRecipeCategory<RollingRecipeWrapper>() {
            @Override public RecipeType<RollingRecipeWrapper> getRecipeType() { return ROLLING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.rolling"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconRollingTray; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, RollingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 8).addItemStack(recipe.paper());
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 8).addItemStack(recipe.weed());
                builder.addSlot(RecipeIngredientRole.INPUT, 45, 8).addItemStack(recipe.additive());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 8).addItemStack(recipe.output());
            }
            @Override public void draw(RollingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 72, 13, 0xFF555555, false);
            }
        });

        this.iconBong = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BONG.get()));

        registration.addRecipeCategories(new IRecipeCategory<BongRecipeWrapper>() {
            @Override public RecipeType<BongRecipeWrapper> getRecipeType() { return BONG_TYPE; }
            @Override public Component getTitle() { return Component.literal("Uso del Bong"); }
            @Override public IDrawable getBackground() { return background; }
            @Override public IDrawable getIcon() { return iconBong; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, BongRecipeWrapper recipe, IFocusGroup focuses) {
                // Ingredientes: Agua (representativo), Hierba, Mechero
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 8).addItemStack(new ItemStack(Items.WATER_BUCKET));
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 8).addItemStack(recipe.weed());
                builder.addSlot(RecipeIngredientRole.INPUT, 45, 8).addItemStack(new ItemStack(Items.FLINT_AND_STEEL));
            }
            @Override public void draw(BongRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔", 68, 13, 0xFF555555, false);
                g.drawString(Minecraft.getInstance().font, "¡A volar!", 90, 13, 0xFF44AA44, false); // Texto de resultado
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // --- RECETAS DE SECADO ---
        List<DryingRecipeWrapper> dryingRecipes = new ArrayList<>();
        ItemStack indicaDry = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.INDICA_BUDS_FRESH.get()), indicaDry));
        ItemStack sativaDry = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.SATIVA_BUDS_FRESH.get()), sativaDry));
        registration.addRecipes(DRYING_TYPE, dryingRecipes);

        // --- RECETAS DE CURADO ---
        List<CuringRecipeWrapper> curingRecipes = new ArrayList<>();
        ItemStack indicaInCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack indicaOutCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(indicaInCure, indicaOutCure));
        ItemStack sativaInCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack sativaOutCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(sativaInCure, sativaOutCure));
        registration.addRecipes(CURING_TYPE, curingRecipes);

        // --- RECETAS DE GRINDER ---
        List<GrindingRecipeWrapper> grindingRecipes = new ArrayList<>();
        ItemStack indicaOutGrind = new ItemStack(ModItems.INDICA_GROUND.get()); indicaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.INDICA_BUDS_DRY.get()), indicaOutGrind));
        ItemStack sativaOutGrind = new ItemStack(ModItems.SATIVA_GROUND.get()); sativaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.SATIVA_BUDS_DRY.get()), sativaOutGrind));
        registration.addRecipes(GRINDING_TYPE, grindingRecipes);

        // --- RECETAS DE BANDEJA DE LIAR ---
        List<RollingRecipeWrapper> rollingRecipes = new ArrayList<>();
        ItemStack vanillaPaper = new ItemStack(Items.PAPER);

        // Indica
        ItemStack iGround = new ItemStack(ModItems.INDICA_GROUND.get()); iGround.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack iJoint = new ItemStack(ModItems.INDICA_JOINT.get()); iJoint.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(vanillaPaper, iGround, ItemStack.EMPTY, iJoint));

        // Sativa
        ItemStack sGround = new ItemStack(ModItems.SATIVA_GROUND.get()); sGround.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack sJoint = new ItemStack(ModItems.SATIVA_JOINT.get()); sJoint.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(vanillaPaper, sGround, ItemStack.EMPTY, sJoint));

        List<BongRecipeWrapper> bongRecipes = new ArrayList<>();
        bongRecipes.add(new BongRecipeWrapper(new ItemStack(ModItems.INDICA_GROUND.get())));
        bongRecipes.add(new BongRecipeWrapper(new ItemStack(ModItems.SATIVA_GROUND.get())));
        registration.addRecipes(BONG_TYPE, bongRecipes);

        // --- INFORMACIÓN DE LA MANTEQUILLA CANÁBICA ---
        registration.addIngredientInfo(
                new ItemStack(ModItems.CANNABUTTER.get()),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                net.minecraft.network.chat.Component.translatable("jei.alteredstates.cannabutter_info")
        );

        // 🧈 1. Receta visual para la Mantequilla Canábica
        NonNullList<Ingredient> butterIngredients = NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(Items.MILK_BUCKET),
                Ingredient.of(Items.BOWL),
                Ingredient.of(ModItems.INDICA_GROUND.get()),
                Ingredient.of(ModItems.INDICA_GROUND.get()),
                Ingredient.of(ModItems.INDICA_GROUND.get())
        );

        ShapelessRecipe fakeButterRecipe = new ShapelessRecipe(
                "alteredstates",
                CraftingBookCategory.MISC,
                new ItemStack(ModItems.CANNABUTTER.get()),
                butterIngredients
        );

        // 🧈 1b. Receta visual para la Mantequilla Canábica (SATIVA)
        NonNullList<Ingredient> sativaButterIngredients = NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(Items.MILK_BUCKET),
                Ingredient.of(Items.BOWL),
                Ingredient.of(ModItems.SATIVA_GROUND.get()),
                Ingredient.of(ModItems.SATIVA_GROUND.get()),
                Ingredient.of(ModItems.SATIVA_GROUND.get())
        );

        ShapelessRecipe fakeSativaButterRecipe = new ShapelessRecipe(
                "alteredstates",
                CraftingBookCategory.MISC,
                new ItemStack(ModItems.CANNABUTTER.get()),
                sativaButterIngredients
        );

        // 🟫 2. Receta visual para el Brownie
        NonNullList<Ingredient> brownieIngredients = NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.CANNABUTTER.get()),
                Ingredient.of(Items.WHEAT), // Trigo
                Ingredient.of(Items.COCOA_BEANS) // Semillas de cacao
        );

        ShapelessRecipe fakeBrownieRecipe = new ShapelessRecipe(
                "alteredstates",
                CraftingBookCategory.MISC,
                new ItemStack(ModItems.BROWNIE.get()),
                brownieIngredients
        );

        // 💉 Inyectar ambas recetas en la categoría de la Mesa de Crafteo de JEI
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_cannabutter_indica"), fakeButterRecipe),
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_cannabutter_sativa"), fakeSativaButterRecipe),
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_brownie"), fakeBrownieRecipe)
        ));

        registration.addRecipes(ROLLING_TYPE, rollingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRYING_RACK.get()), DRYING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CURING_JAR.get()), CURING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GRINDER.get()), GRINDING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROLLING_TRAY.get()), ROLLING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BONG.get()), BONG_TYPE);
    }

    public record DryingRecipeWrapper(ItemStack input, ItemStack output) {}
    public record CuringRecipeWrapper(ItemStack input, ItemStack output) {}
    public record GrindingRecipeWrapper(ItemStack input, ItemStack output) {}
    public record RollingRecipeWrapper(ItemStack paper, ItemStack weed, ItemStack additive, ItemStack output) {}
    public record BongRecipeWrapper(ItemStack weed) {}
}