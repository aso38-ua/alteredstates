package com.alteredstates.registry;

import com.alteredstates.AlteredStates;
import com.alteredstates.item.CannabisBudItem;
import com.alteredstates.item.GrinderItem;
import com.alteredstates.item.JointItem;
import com.alteredstates.item.RollingTrayItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AlteredStates.MOD_ID);

    // ════════════════════════════════════════════════════════════
    //  CANNABIS — Cultivo
    // ════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> INDICA_SEEDS = ITEMS.register("indica_seeds",
            () -> new ItemNameBlockItem(ModBlocks.INDICA_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> SATIVA_SEEDS = ITEMS.register("sativa_seeds",
            () -> new ItemNameBlockItem(ModBlocks.SATIVA_CROP.get(), new Item.Properties()));
    // ════════════════════════════════════════════════════════════
    //  CANNABIS — Cogollos
    // ════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> INDICA_BUDS_FRESH = ITEMS.register("indica_buds_fresh",
            () -> new CannabisBudItem(new Item.Properties()));
    public static final DeferredItem<Item> SATIVA_BUDS_FRESH = ITEMS.register("sativa_buds_fresh",
            () -> new CannabisBudItem(new Item.Properties()));

    public static final DeferredItem<Item> CANNABIS_TRIMMING = ITEMS.register("cannabis_trimming",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> INDICA_BUDS_DRY = ITEMS.register("indica_buds_dry",
            () -> new CannabisBudItem(new Item.Properties()));

    public static final DeferredItem<Item> SATIVA_BUDS_DRY = ITEMS.register("sativa_buds_dry",
            () -> new CannabisBudItem(new Item.Properties()));

    public static final DeferredItem<Item> INDICA_GROUND = ITEMS.register("indica_ground",
            () -> new CannabisBudItem(new Item.Properties()));

    public static final DeferredItem<Item> SATIVA_GROUND = ITEMS.register("sativa_ground",
            () -> new CannabisBudItem(new Item.Properties()));

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
}