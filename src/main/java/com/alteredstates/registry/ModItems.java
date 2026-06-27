package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.food.FoodProperties;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AlteredStates.MOD_ID);

    // ════════════════════════════════════════════════════════════
    //  Cultivo
    // ════════════════════════════════════════════════════════════

    //  CANNABIS
    public static final DeferredItem<Item> INDICA_SEEDS = ITEMS.register("indica_seeds",
            () -> new ItemNameBlockItem(ModBlocks.INDICA_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> SATIVA_SEEDS = ITEMS.register("sativa_seeds",
            () -> new ItemNameBlockItem(ModBlocks.SATIVA_CROP.get(), new Item.Properties()));

    //  TOBACCO
    public static final DeferredItem<Item> TOBACCO_SEEDS = ITEMS.register("tobacco_seeds",
            () -> new ItemNameBlockItem(ModBlocks.TOBACCO_CROP.get(), new Item.Properties()));

    // ════════════════════════════════════════════════════════════
    //  PRODUCTOS
    // ════════════════════════════════════════════════════════════

    //  CANNABIS
    public static final DeferredItem<Item> INDICA_BUDS_FRESH = ITEMS.register("indica_buds_fresh",
            () -> new FreshBudItem(new Item.Properties(), true));

    public static final DeferredItem<Item> SATIVA_BUDS_FRESH = ITEMS.register("sativa_buds_fresh",
            () -> new FreshBudItem(new Item.Properties(), false));

    public static final DeferredItem<Item> CANNABIS_TRIMMING = ITEMS.register("cannabis_trimming",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> INDICA_BUDS_DRY = ITEMS.register("indica_buds_dry",
            () -> new DryBudItem(new Item.Properties(), true));

    public static final DeferredItem<Item> SATIVA_BUDS_DRY = ITEMS.register("sativa_buds_dry",
            () -> new DryBudItem(new Item.Properties(), false));

    // La hierba picada usa la nueva clase (puede liarse y fumarse en bong)
    public static final DeferredItem<Item> INDICA_GROUND = ITEMS.register("indica_ground",
            () -> new GroundWeedItem(new Item.Properties(), true));

    public static final DeferredItem<Item> SATIVA_GROUND = ITEMS.register("sativa_ground",
            () -> new GroundWeedItem(new Item.Properties(), false));

    //  TOBACCO
    public static final DeferredItem<Item> CAPOTE_FRESH = ITEMS.register("capote_fresh",
            () -> new TobaccoFreshLeafItem(new Item.Properties(), TobaccoLeafType.CAPOTE));

    public static final DeferredItem<Item> CAPA_FRESH = ITEMS.register("capa_fresh",
            () -> new TobaccoFreshLeafItem(new Item.Properties(), TobaccoLeafType.CAPA));

    public static final DeferredItem<Item> TRIPA_FRESH = ITEMS.register("tripa_fresh",
            () -> new TobaccoFreshLeafItem(new Item.Properties(), TobaccoLeafType.TRIPA));

    public static final DeferredItem<Item> CAPOTE_DRY = ITEMS.register("capote_dry",
            () -> new TobaccoDryLeafItem(new Item.Properties(), TobaccoLeafType.CAPOTE));

    public static final DeferredItem<Item> CAPA_DRY = ITEMS.register("capa_dry",
            () -> new TobaccoDryLeafItem(new Item.Properties(), TobaccoLeafType.CAPA));

    public static final DeferredItem<Item> TRIPA_DRY = ITEMS.register("tripa_dry",
            () -> new TobaccoDryLeafItem(new Item.Properties(), TobaccoLeafType.TRIPA));

    public static final DeferredItem<Item> ROLLING_TOBACCO = ITEMS.register("rolling_tobacco",
            () -> new Item(new Item.Properties()));
    // ════════════════════════════════════════════════════════════
    //  CANNABIS — Productos finales
    // ════════════════════════════════════════════════════════════
    //   ROLLING_PAPER, BLUNT_WRAP
    //   JOINT, BLUNT, SPLIFF, BLUNT_MOONROCK
    //   WOODEN_PIPE, GRINDER, CURING_JAR
    //   CANNABUTTER, BROWNIE, CANNABIS_COOKIE, CANNABIS_TEA
    // Durabilidad de 128 usos y stacks de 1 sola unidad (máximo para herramientas)
    public static final DeferredItem<GrinderItem> GRINDER = ITEMS.register("grinder",
            () -> new GrinderItem(new Item.Properties().durability(128).stacksTo(1)));

    public static final DeferredItem<Item> DRYING_RACK = ITEMS.register("drying_rack",
            () -> new BlockItem(ModBlocks.DRYING_RACK.get(), new Item.Properties()));

    public static final DeferredItem<Item> CURING_JAR = ITEMS.register("curing_jar",
            () -> new BlockItem(ModBlocks.CURING_JAR.get(), new Item.Properties()));

    // El bloque-item de la bandeja (Que lanza las instrucciones de Shift)
    public static final DeferredItem<Item> ROLLING_TRAY = ITEMS.register("rolling_tray",
            () -> new RollingTrayItem(ModBlocks.ROLLING_TRAY.get(), new Item.Properties()));

    public static final DeferredItem<Item> INDICA_JOINT = ITEMS.register("indica_joint",
            () -> new JointItem(new Item.Properties(), true)); // true = es Indica

    public static final DeferredItem<Item> SATIVA_JOINT = ITEMS.register("sativa_joint",
            () -> new JointItem(new Item.Properties(), false)); // false = es Sativa

    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> BONG = ITEMS.register("bong",
            () -> new net.minecraft.world.item.BlockItem(com.alteredstates.registry.ModBlocks.BONG.get(), new net.minecraft.world.item.Item.Properties()));

    // 🧈 Mantequilla de Cannabis (Ingrediente base para cocinar)
    public static final DeferredItem<CannabutterItem> CANNABUTTER = ITEMS.register("cannabutter",
            () -> new CannabutterItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build()
            )));

    // 🍫 Brownie (La recompensa deliciosa)
    public static final DeferredItem<Item> BROWNIE =
            ITEMS.register("brownie", () -> new EdibleWeedItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build()
            )));

    //============ TABACO ====================
    public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette",
            () -> new CigaretteItem(new Item.Properties()));

    public static final DeferredItem<Item> TORITO_CIGAR = ITEMS.register("torito_cigar",
            () -> new CigarItem(new Item.Properties(), CigarType.TORITO));

    public static final DeferredItem<Item> LANCERO_CIGAR = ITEMS.register("lancero_cigar",
            () -> new CigarItem(new Item.Properties(), CigarType.LANCERO));

    public static final DeferredItem<Item> ESPLENDIDO_CIGAR = ITEMS.register("esplendido_cigar",
            () -> new CigarItem(new Item.Properties(), CigarType.ESPLENDIDO));

    public static final DeferredItem<Item> DON_JAVIER_CIGAR = ITEMS.register("don_javier_cigar",
            () -> new CigarItem(new Item.Properties(), CigarType.DON_JAVIER));

    // ════════════════════════════════════════════════════════════
    //  SETAS — Cultivo
    // ════════════════════════════════════════════════════════════
    //   MYSTICA_FRESH, MYSTICA_DRY
    //   ONIRICA_FRESH, ONIRICA_DRY
    //   CHAOS_FRESH,   CHAOS_DRY
    //   SPORE_PRINT_MYSTICA, SPORE_PRINT_ONIRICA, SPORE_PRINT_CHAOS
    //   EMPTY_CAPSULE

    // ════════════════════════════════════════════════════════════
    //  SETAS — Procesados y productos
    // ════════════════════════════════════════════════════════════
    //   MUSHROOM_POWDER, MUSHROOM_TEA, MUSHROOM_CAPSULE
    //   MUSHROOM_MICRODOSE, MUSHROOM_CHOCOLATE
    //   COMPOSTED_SUBSTRATE_ITEM, ENRICHED_SUBSTRATE_ITEM

    // --- SUSTRATOS DE HONGOS ---
    public static final DeferredItem<Item> COMPOSTED_SUBSTRATE_ITEM = ITEMS.register("composted_substrate",
            () -> new BlockItem(ModBlocks.COMPOSTED_SUBSTRATE.get(), new Item.Properties()));

    public static final DeferredItem<Item> ENRICHED_SUBSTRATE_ITEM = ITEMS.register("enriched_substrate",
            () -> new BlockItem(ModBlocks.ENRICHED_SUBSTRATE.get(), new Item.Properties()));

    // Esporas (La semilla que planta el bloque)
    public static final DeferredItem<Item> MYSTIC_MUSHROOM_SPORES = ITEMS.register("mystic_mushroom_spores",
            () -> new net.minecraft.world.item.ItemNameBlockItem(ModBlocks.MYSTIC_MUSHROOM_CROP.get(), new Item.Properties()));

    // Seta Fresca (El drop al cosechar. Más adelante implementará IDryable)
    public static final DeferredItem<Item> MYSTIC_MUSHROOM_FRESH = ITEMS.register("mystic_mushroom_fresh",
            () -> new MysticMushroomItem(new Item.Properties()));

    // Seta Seca (Curable y Molible)
    public static final DeferredItem<Item> MYSTIC_MUSHROOM_DRIED = ITEMS.register("mystic_mushroom_dried",
            () -> new MysticMushroomDriedItem(new Item.Properties()));

    // Seta Triturada (El resultado del Grinder, más adelante implementará IProduct para tés/comida)
    public static final DeferredItem<Item> MYSTIC_MUSHROOM_GROUND = ITEMS.register("mystic_mushroom_ground",
            () -> new Item(new Item.Properties()));


    // ===================== EXTRAS ====================
    public static final DeferredItem<Item> HORSE_SEMEN = ITEMS.register("horse_semen",
            () -> new HorseSemenItem(new Item.Properties()));

}