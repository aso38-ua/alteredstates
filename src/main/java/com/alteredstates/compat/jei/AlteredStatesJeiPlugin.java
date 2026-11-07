package com.alteredstates.compat.jei;

import com.alteredstates.AlteredStates;
import com.alteredstates.item.CuttingAgents;
import com.alteredstates.registry.ModBlocks;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModGasoline;
import com.alteredstates.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
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
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    public static final RecipeType<CauldronRecipeWrapper> CAULDRON_TYPE = RecipeType.create(AlteredStates.MOD_ID, "cauldron", CauldronRecipeWrapper.class);
    public static final RecipeType<LabFurnaceRecipeWrapper> LAB_FURNACE_TYPE = RecipeType.create(AlteredStates.MOD_ID, "lab_furnace", LabFurnaceRecipeWrapper.class);
    public static final RecipeType<CuttingRecipeWrapper> CUTTING_TYPE = RecipeType.create(AlteredStates.MOD_ID, "cutting", CuttingRecipeWrapper.class);

    private IDrawable backgroundSmall;
    private IDrawable backgroundCauldron;
    private IDrawable backgroundFurnace;
    private IDrawable iconDrying;
    private IDrawable iconCuring;
    private IDrawable iconGrinder;
    private IDrawable iconRollingTray;
    private IDrawable iconBong;
    private IDrawable iconCauldron;
    private IDrawable iconLabFurnace;
    private IDrawable iconCuttingMirror;

    @Override public ResourceLocation getPluginUid() { return PLUGIN_ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        this.backgroundSmall = guiHelper.createBlankDrawable(140, 35);
        this.backgroundCauldron = guiHelper.createBlankDrawable(160, 55);
        this.backgroundFurnace = guiHelper.createBlankDrawable(140, 40);

        this.iconDrying = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DRYING_RACK.get()));
        this.iconCuring = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CURING_JAR.get()));
        this.iconGrinder = guiHelper.createDrawableItemStack(new ItemStack(ModItems.GRINDER.get()));
        this.iconRollingTray = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ROLLING_TRAY.get()));
        this.iconBong = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BONG.get()));
        this.iconCauldron = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.INDUSTRIAL_CAULDRON.get()));
        this.iconLabFurnace = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.LAB_FURNACE.get()));
        this.iconCuttingMirror = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CUTTING_MIRROR.get()));

        // 1. SECADERO
        registration.addRecipeCategories(new IRecipeCategory<DryingRecipeWrapper>() {
            @Override public RecipeType<DryingRecipeWrapper> getRecipeType() { return DRYING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.drying"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
            @Override public IDrawable getIcon() { return iconDrying; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }
            @Override public void draw(DryingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });

        // 2. CURADO
        registration.addRecipeCategories(new IRecipeCategory<CuringRecipeWrapper>() {
            @Override public RecipeType<CuringRecipeWrapper> getRecipeType() { return CURING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.curing"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
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

        // 3. GRINDER
        registration.addRecipeCategories(new IRecipeCategory<GrindingRecipeWrapper>() {
            @Override public RecipeType<GrindingRecipeWrapper> getRecipeType() { return GRINDING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.grinding"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
            @Override public IDrawable getIcon() { return iconGrinder; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 15, 8).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }
            @Override public void draw(GrindingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 52, 13, 0xFF555555, false);
            }
        });

        // 4. BANDEJA DE LIAR
        registration.addRecipeCategories(new IRecipeCategory<RollingRecipeWrapper>() {
            @Override public RecipeType<RollingRecipeWrapper> getRecipeType() { return ROLLING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.rolling"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
            @Override public IDrawable getIcon() { return iconRollingTray; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, RollingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 8).addItemStack(recipe.slot1());
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 8).addItemStack(recipe.slot2());
                builder.addSlot(RecipeIngredientRole.INPUT, 45, 8).addItemStack(recipe.slot3());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 8).addItemStack(recipe.output());
            }
            @Override public void draw(RollingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 72, 13, 0xFF555555, false);
            }
        });

        // 5. BONG
        registration.addRecipeCategories(new IRecipeCategory<BongRecipeWrapper>() {
            @Override public RecipeType<BongRecipeWrapper> getRecipeType() { return BONG_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.bong_title"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
            @Override public IDrawable getIcon() { return iconBong; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, BongRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 8).addItemStack(new ItemStack(Items.WATER_BUCKET));
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 8).addItemStack(recipe.weed());
                builder.addSlot(RecipeIngredientRole.INPUT, 45, 8).addItemStack(new ItemStack(Items.FLINT_AND_STEEL));
            }
            @Override public void draw(BongRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔", 68, 13, 0xFF555555, false);
                g.drawString(Minecraft.getInstance().font, Component.translatable("jei.alteredstates.bong_fly"), 90, 13, 0xFF44AA44, false);
            }
        });

        // 6. CALDERO INDUSTRIAL (Gasolina y Pasta de Coca)
        registration.addRecipeCategories(new IRecipeCategory<CauldronRecipeWrapper>() {
            @Override public RecipeType<CauldronRecipeWrapper> getRecipeType() { return CAULDRON_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.industrial_cauldron"); }
            @Override public IDrawable getBackground() { return backgroundCauldron; }
            @Override public IDrawable getIcon() { return iconCauldron; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, CauldronRecipeWrapper recipe, IFocusGroup focuses) {
                // Fluido base
                builder.addSlot(RecipeIngredientRole.INPUT, 8, 16).addItemStack(recipe.fluidInput());
                // Ingredientes sólidos (hasta 2 slots)
                builder.addSlot(RecipeIngredientRole.INPUT, 36, 6).addItemStack(recipe.item1());
                if (!recipe.item2().isEmpty()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, 36, 28).addItemStack(recipe.item2());
                }
                // Catalizador / calor debajo
                builder.addSlot(RecipeIngredientRole.CATALYST, 70, 32).addItemStack(recipe.heatSource());
                // Salida
                builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 16).addItemStack(recipe.output());
            }
            @Override public void draw(CauldronRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔", 96, 20, 0xFF555555, false);
                g.drawString(Minecraft.getInstance().font, r.timeInfo(), 60, 4, 0xFF777777, false);
                g.drawString(Minecraft.getInstance().font, "🔥", 73, 18, 0xFFFFAA00, false);
            }
        });

        // 7. HORNO DE LABORATORIO (Cocción de Pasta de Coca a Roca)
        registration.addRecipeCategories(new IRecipeCategory<LabFurnaceRecipeWrapper>() {
            @Override public RecipeType<LabFurnaceRecipeWrapper> getRecipeType() { return LAB_FURNACE_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.lab_furnace"); }
            @Override public IDrawable getBackground() { return backgroundFurnace; }
            @Override public IDrawable getIcon() { return iconLabFurnace; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, LabFurnaceRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 16, 12).addItemStack(recipe.input());
                builder.addSlot(RecipeIngredientRole.CATALYST, 58, 22).addItemStack(new ItemStack(Items.CAMPFIRE));
                builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 12).addItemStack(recipe.output());
            }
            @Override public void draw(LabFurnaceRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔", 78, 16, 0xFF555555, false);
                g.drawString(Minecraft.getInstance().font, "🔥 30s", 54, 8, 0xFFFFAA00, false);
            }
        });

        // 8. CORTADO DE COCAÍNA (Espejo o Mesa de trabajo)
        registration.addRecipeCategories(new IRecipeCategory<CuttingRecipeWrapper>() {
            @Override public RecipeType<CuttingRecipeWrapper> getRecipeType() { return CUTTING_TYPE; }
            @Override public Component getTitle() { return Component.translatable("jei.alteredstates.cutting"); }
            @Override public IDrawable getBackground() { return backgroundSmall; }
            @Override public IDrawable getIcon() { return iconCuttingMirror; }
            @Override public void setRecipe(IRecipeLayoutBuilder builder, CuttingRecipeWrapper recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 10, 8).addItemStack(recipe.powder());
                builder.addSlot(RecipeIngredientRole.INPUT, 36, 8).addItemStack(recipe.agent());
                builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 8).addItemStack(recipe.output());
            }
            @Override public void draw(CuttingRecipeWrapper r, IRecipeSlotsView v, GuiGraphics g, double x, double y) {
                g.drawString(Minecraft.getInstance().font, "➔➔➔", 64, 13, 0xFF555555, false);
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // --- 1. RECETAS DE SECADO ---
        List<DryingRecipeWrapper> dryingRecipes = new ArrayList<>();
        // Cannabis
        ItemStack indicaDry = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.INDICA_BUDS_FRESH.get()), indicaDry));
        ItemStack sativaDry = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.SATIVA_BUDS_FRESH.get()), sativaDry));

        // Tabaco: Capote, Capa y Tripa frescas -> secas
        ItemStack capoteDry = new ItemStack(ModItems.CAPOTE_DRY.get()); capoteDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.CAPOTE_FRESH.get()), capoteDry));
        ItemStack capaDry = new ItemStack(ModItems.CAPA_DRY.get()); capaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.CAPA_FRESH.get()), capaDry));
        ItemStack tripaDry = new ItemStack(ModItems.TRIPA_DRY.get()); tripaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.TRIPA_FRESH.get()), tripaDry));

        // Coca: Hoja fresca -> seca
        ItemStack cocaDry = new ItemStack(ModItems.COCA_LEAF_DRY.get()); cocaDry.set(ModDataComponentTypes.QUALITY.get(), 1);
        dryingRecipes.add(new DryingRecipeWrapper(new ItemStack(ModItems.COCA_LEAF.get()), cocaDry));

        registration.addRecipes(DRYING_TYPE, dryingRecipes);

        // --- 2. RECETAS DE CURADO ---
        List<CuringRecipeWrapper> curingRecipes = new ArrayList<>();
        ItemStack indicaInCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack indicaOutCure = new ItemStack(ModItems.INDICA_BUDS_DRY.get()); indicaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(indicaInCure, indicaOutCure));

        ItemStack sativaInCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack sativaOutCure = new ItemStack(ModItems.SATIVA_BUDS_DRY.get()); sativaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(sativaInCure, sativaOutCure));

        ItemStack capoteInCure = new ItemStack(ModItems.CAPOTE_DRY.get()); capoteInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack capoteOutCure = new ItemStack(ModItems.CAPOTE_DRY.get()); capoteOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(capoteInCure, capoteOutCure));

        ItemStack capaInCure = new ItemStack(ModItems.CAPA_DRY.get()); capaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack capaOutCure = new ItemStack(ModItems.CAPA_DRY.get()); capaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(capaInCure, capaOutCure));

        ItemStack tripaInCure = new ItemStack(ModItems.TRIPA_DRY.get()); tripaInCure.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack tripaOutCure = new ItemStack(ModItems.TRIPA_DRY.get()); tripaOutCure.set(ModDataComponentTypes.QUALITY.get(), 2);
        curingRecipes.add(new CuringRecipeWrapper(tripaInCure, tripaOutCure));

        registration.addRecipes(CURING_TYPE, curingRecipes);

        // --- 3. RECETAS DE GRINDER ---
        List<GrindingRecipeWrapper> grindingRecipes = new ArrayList<>();
        ItemStack indicaOutGrind = new ItemStack(ModItems.INDICA_GROUND.get()); indicaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.INDICA_BUDS_DRY.get()), indicaOutGrind));

        ItemStack sativaOutGrind = new ItemStack(ModItems.SATIVA_GROUND.get()); sativaOutGrind.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.SATIVA_BUDS_DRY.get()), sativaOutGrind));

        ItemStack rollingTobaccoOut = new ItemStack(ModItems.ROLLING_TOBACCO.get()); rollingTobaccoOut.set(ModDataComponentTypes.QUALITY.get(), 1);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.CAPOTE_DRY.get()), rollingTobaccoOut));
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.CAPA_DRY.get()), rollingTobaccoOut));
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.TRIPA_DRY.get()), rollingTobaccoOut));

        // Coca: Roca -> Polvo
        ItemStack cocaPowder = new ItemStack(ModItems.COCAINE_POWDER.get()); cocaPowder.set(ModDataComponentTypes.QUALITY.get(), 2);
        grindingRecipes.add(new GrindingRecipeWrapper(new ItemStack(ModItems.COCAINE_ROCK.get()), cocaPowder));

        registration.addRecipes(GRINDING_TYPE, grindingRecipes);

        // --- 4. RECETAS DE BANDEJA DE LIAR ---
        List<RollingRecipeWrapper> rollingRecipes = new ArrayList<>();
        ItemStack vanillaPaper = new ItemStack(Items.PAPER);

        ItemStack iGround = new ItemStack(ModItems.INDICA_GROUND.get()); iGround.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack iJoint = new ItemStack(ModItems.ROLLED_JOINT.get());
        iJoint.set(ModDataComponentTypes.QUALITY.get(), 1);
        iJoint.set(ModDataComponentTypes.CONTENT_TYPE.get(), "indica");
        rollingRecipes.add(new RollingRecipeWrapper(vanillaPaper, iGround, ItemStack.EMPTY, iJoint));

        ItemStack sGround = new ItemStack(ModItems.SATIVA_GROUND.get()); sGround.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack sJoint = new ItemStack(ModItems.ROLLED_JOINT.get());
        sJoint.set(ModDataComponentTypes.QUALITY.get(), 1);
        sJoint.set(ModDataComponentTypes.CONTENT_TYPE.get(), "sativa");
        rollingRecipes.add(new RollingRecipeWrapper(vanillaPaper, sGround, ItemStack.EMPTY, sJoint));

        ItemStack rollTobaccoIn = new ItemStack(ModItems.ROLLING_TOBACCO.get()); rollTobaccoIn.set(ModDataComponentTypes.QUALITY.get(), 1);
        ItemStack cigarette = new ItemStack(ModItems.CIGARETTE.get());
        cigarette.set(ModDataComponentTypes.QUALITY.get(), 1);
        cigarette.set(ModDataComponentTypes.CONTENT_TYPE.get(), "Rolling tobacco");
        rollingRecipes.add(new RollingRecipeWrapper(vanillaPaper, rollTobaccoIn, ItemStack.EMPTY, cigarette));

        ItemStack lanceroCigar = new ItemStack(ModItems.LANCERO_CIGAR.get()); lanceroCigar.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(new ItemStack(ModItems.CAPOTE_DRY.get()), new ItemStack(ModItems.CAPOTE_DRY.get()), new ItemStack(ModItems.CAPA_DRY.get()), lanceroCigar));

        ItemStack toritoCigar = new ItemStack(ModItems.TORITO_CIGAR.get()); toritoCigar.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(new ItemStack(ModItems.CAPA_DRY.get()), new ItemStack(ModItems.CAPA_DRY.get()), new ItemStack(ModItems.TRIPA_DRY.get()), toritoCigar));

        ItemStack esplendidoCigar = new ItemStack(ModItems.ESPLENDIDO_CIGAR.get()); esplendidoCigar.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(new ItemStack(ModItems.TRIPA_DRY.get()), new ItemStack(ModItems.TRIPA_DRY.get()), new ItemStack(ModItems.CAPOTE_DRY.get()), esplendidoCigar));

        ItemStack donJavierCigar = new ItemStack(ModItems.DON_JAVIER_CIGAR.get()); donJavierCigar.set(ModDataComponentTypes.QUALITY.get(), 1);
        rollingRecipes.add(new RollingRecipeWrapper(new ItemStack(ModItems.CAPA_DRY.get()), new ItemStack(ModItems.CAPOTE_DRY.get()), new ItemStack(ModItems.TRIPA_DRY.get()), donJavierCigar));

        registration.addRecipes(ROLLING_TYPE, rollingRecipes);

        // --- 5. RECETAS DE BONG ---
        List<BongRecipeWrapper> bongRecipes = new ArrayList<>();
        bongRecipes.add(new BongRecipeWrapper(new ItemStack(ModItems.INDICA_GROUND.get())));
        bongRecipes.add(new BongRecipeWrapper(new ItemStack(ModItems.SATIVA_GROUND.get())));
        bongRecipes.add(new BongRecipeWrapper(new ItemStack(ModItems.ROLLING_TOBACCO.get())));
        registration.addRecipes(BONG_TYPE, bongRecipes);

        // --- 6. RECETAS DEL CALDERO INDUSTRIAL ---
        List<CauldronRecipeWrapper> cauldronRecipes = new ArrayList<>();
        // a) Receta de Gasolina Cruda (1000 mB Agua + 4 Carbón + 2 Blaze -> 1000 mB Gasolina)
        cauldronRecipes.add(new CauldronRecipeWrapper(
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.COAL, 4),
                new ItemStack(Items.BLAZE_POWDER, 2),
                new ItemStack(ModGasoline.GASOLINE_BUCKET.get()),
                new ItemStack(Items.CAMPFIRE),
                "~10 min"
        ));
        // b) Receta de Pasta de Coca (500 mB Gasolina + 8 Hojas secas de coca -> 2 Pasta de coca)
        ItemStack pasteOutput = new ItemStack(ModItems.COCA_PASTE.get(), 2);
        pasteOutput.set(ModDataComponentTypes.QUALITY.get(), 2);
        cauldronRecipes.add(new CauldronRecipeWrapper(
                new ItemStack(ModGasoline.GASOLINE_BUCKET.get()),
                new ItemStack(ModItems.COCA_LEAF_DRY.get(), 8),
                ItemStack.EMPTY,
                pasteOutput,
                new ItemStack(Items.CAMPFIRE),
                "~3 min"
        ));
        registration.addRecipes(CAULDRON_TYPE, cauldronRecipes);

        // --- 7. RECETAS DEL HORNO DE LABORATORIO ---
        List<LabFurnaceRecipeWrapper> furnaceRecipes = new ArrayList<>();
        ItemStack pasteIn = new ItemStack(ModItems.COCA_PASTE.get(), 1);
        pasteIn.set(ModDataComponentTypes.QUALITY.get(), 2);
        ItemStack rockOut = new ItemStack(ModItems.COCAINE_ROCK.get(), 1);
        rockOut.set(ModDataComponentTypes.QUALITY.get(), 2);
        furnaceRecipes.add(new LabFurnaceRecipeWrapper(pasteIn, rockOut));
        registration.addRecipes(LAB_FURNACE_TYPE, furnaceRecipes);

        // --- 8. RECETAS DE CORTADO DE COCAÍNA ---
        List<CuttingRecipeWrapper> cuttingRecipes = new ArrayList<>();
        // Calidad 2 base (Normal) cortada con diferentes aditivos:
        ItemStack basePowder = new ItemStack(ModItems.COCAINE_POWDER.get(), 1);
        basePowder.set(ModDataComponentTypes.QUALITY.get(), 2);

        // Q0: Carne podrida -> 4 dosis Q0
        ItemStack outQ0 = new ItemStack(ModItems.COCAINE_POWDER.get(), 4); outQ0.set(ModDataComponentTypes.QUALITY.get(), 0);
        cuttingRecipes.add(new CuttingRecipeWrapper(basePowder, new ItemStack(Items.ROTTEN_FLESH), outQ0));

        // Q1: Azúcar -> 3 dosis Q1
        ItemStack outQ1 = new ItemStack(ModItems.COCAINE_POWDER.get(), 3); outQ1.set(ModDataComponentTypes.QUALITY.get(), 1);
        cuttingRecipes.add(new CuttingRecipeWrapper(basePowder, new ItemStack(Items.SUGAR), outQ1));

        // Q2: Redstone -> 2 dosis Q2
        ItemStack outQ2 = new ItemStack(ModItems.COCAINE_POWDER.get(), 2); outQ2.set(ModDataComponentTypes.QUALITY.get(), 2);
        cuttingRecipes.add(new CuttingRecipeWrapper(basePowder, new ItemStack(Items.REDSTONE), outQ2));

        // Q3: Cuarzo -> 2 dosis Q3
        ItemStack outQ3 = new ItemStack(ModItems.COCAINE_POWDER.get(), 2); outQ3.set(ModDataComponentTypes.QUALITY.get(), 3);
        cuttingRecipes.add(new CuttingRecipeWrapper(basePowder, new ItemStack(Items.QUARTZ), outQ3));

        // Q4: Perla de Ender -> 1 dosis Q4
        ItemStack outQ4 = new ItemStack(ModItems.COCAINE_POWDER.get(), 1); outQ4.set(ModDataComponentTypes.QUALITY.get(), 4);
        cuttingRecipes.add(new CuttingRecipeWrapper(basePowder, new ItemStack(Items.ENDER_PEARL), outQ4));

        registration.addRecipes(CUTTING_TYPE, cuttingRecipes);

        // --- INFORMACIÓN DE LA MANTEQUILLA CANÁBICA ---
        registration.addIngredientInfo(
                new ItemStack(ModItems.CANNABUTTER.get()),
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.translatable("jei.alteredstates.cannabutter_info")
        );

        // Mantequilla Canábica visual
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

        NonNullList<Ingredient> brownieIngredients = NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.CANNABUTTER.get()),
                Ingredient.of(Items.WHEAT),
                Ingredient.of(Items.COCOA_BEANS)
        );

        ShapelessRecipe fakeBrownieRecipe = new ShapelessRecipe(
                "alteredstates",
                CraftingBookCategory.MISC,
                new ItemStack(ModItems.BROWNIE.get()),
                brownieIngredients
        );

        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_cannabutter_indica"), fakeButterRecipe),
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_cannabutter_sativa"), fakeSativaButterRecipe),
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "jei_visual_brownie"), fakeBrownieRecipe)
        ));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRYING_RACK.get()), DRYING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CURING_JAR.get()), CURING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GRINDER.get()), GRINDING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROLLING_TRAY.get()), ROLLING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BONG.get()), BONG_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INDUSTRIAL_CAULDRON.get()), CAULDRON_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LAB_FURNACE.get()), LAB_FURNACE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CUTTING_MIRROR.get()), CUTTING_TYPE);
    }

    public record DryingRecipeWrapper(ItemStack input, ItemStack output) {}
    public record CuringRecipeWrapper(ItemStack input, ItemStack output) {}
    public record GrindingRecipeWrapper(ItemStack input, ItemStack output) {}
    public record RollingRecipeWrapper(ItemStack slot1, ItemStack slot2, ItemStack slot3, ItemStack output) {
        public ItemStack paper() { return slot1; }
        public ItemStack weed() { return slot2; }
        public ItemStack additive() { return slot3; }
    }
    public record BongRecipeWrapper(ItemStack weed) {}
    public record CauldronRecipeWrapper(ItemStack fluidInput, ItemStack item1, ItemStack item2, ItemStack output, ItemStack heatSource, String timeInfo) {}
    public record LabFurnaceRecipeWrapper(ItemStack input, ItemStack output) {}
    public record CuttingRecipeWrapper(ItemStack powder, ItemStack agent, ItemStack output) {}
}
