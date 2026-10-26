package com.alteredstates.block.entity;

import com.alteredstates.AlteredStates;
import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Caldero industrial (sin GUI). Todo se hace con click derecho:
 *  - Agua (cubo) + 4 carbón + 2 polvo de blaze + fuego debajo (~10 min) -> 1000 mB de gasolina cruda
 *  - Gasolina + 8 hojas secas de coca + fuego debajo (~3 min)          -> 2 pasta de coca
 * Acepta por tuberías agua y cualquier fluido de la tag alteredstates:solvent_fluids.
 */
public class IndustrialCauldronBlockEntity extends BlockEntity {

    public static final TagKey<Fluid> SOLVENT_FLUIDS = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(AlteredStates.MOD_ID, "solvent_fluids"));

    public static final int WATER_NEEDED = 1000;
    public static final int GASOLINE_YIELD = 1000;
    public static final int GASOLINE_MAX = 2000;
    public static final int PASTE_GASOLINE_COST = 500;
    public static final int COAL_NEEDED = 4;
    public static final int BLAZE_NEEDED = 2;
    public static final int LEAVES_NEEDED = 8;
    public static final int PASTE_YIELD = 2;
    public static final int GASOLINE_TICKS = 12000; // 10 min
    public static final int PASTE_TICKS = 3600;     // 3 min

    public enum Recipe { NONE, GASOLINE, PASTE }

    private int waterMb = 0;
    private int gasolineMb = 0;
    private final List<ItemStack> ingredients = new ArrayList<>();
    private ItemStack output = ItemStack.EMPTY;
    private int progress = 0;
    private boolean brewing = false; // sincronizado con el cliente (partículas)
    private int heat = 0;            // 0 = frío, 1 = hoguera, 2 = Blaze Burner potenciado

    public IndustrialCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_CAULDRON.get(), pos, state);
    }

    // ───────────── Getters (renderer / bloque) ─────────────
    public int getWaterMb() { return waterMb; }
    public int getGasolineMb() { return gasolineMb; }
    public ItemStack getOutput() { return output; }
    public boolean isBrewing() { return brewing; }
    public boolean hasGasoline() { return gasolineMb > 0; }
    public boolean isEmpty() { return waterMb == 0 && gasolineMb == 0 && ingredients.isEmpty() && output.isEmpty(); }
    public boolean hasIngredients() { return !ingredients.isEmpty(); }
    public List<ItemStack> getIngredients() { return ingredients; }

    /** Una entrada por unidad (para dibujar cada ítem flotando). */
    public List<ItemStack> getIngredientsFlat() {
        List<ItemStack> flat = new ArrayList<>();
        for (ItemStack s : ingredients) {
            for (int i = 0; i < s.getCount(); i++) flat.add(s.copyWithCount(1));
        }
        return flat;
    }

    // ───────────── Reglas de receta ─────────────
    private static boolean isCoal(ItemStack s) { return s.is(ItemTags.COALS); }
    private static boolean isBlaze(ItemStack s) { return s.is(Items.BLAZE_POWDER); }
    private static boolean isLeaf(ItemStack s) { return s.is(ModItems.COCA_LEAF_DRY.get()); }

    private int count(java.util.function.Predicate<ItemStack> p) {
        int n = 0;
        for (ItemStack s : ingredients) if (p.test(s)) n += s.getCount();
        return n;
    }

    private boolean waterStage() { return waterMb > 0 && gasolineMb == 0; }
    private boolean gasolineStage() { return gasolineMb > 0 && waterMb == 0; }

    public Recipe currentRecipe() {
        if (!output.isEmpty()) return Recipe.NONE;
        if (waterMb >= WATER_NEEDED && gasolineMb == 0
                && count(IndustrialCauldronBlockEntity::isCoal) == COAL_NEEDED
                && count(IndustrialCauldronBlockEntity::isBlaze) == BLAZE_NEEDED) return Recipe.GASOLINE;
        if (gasolineMb >= PASTE_GASOLINE_COST && waterMb == 0
                && count(IndustrialCauldronBlockEntity::isLeaf) == LEAVES_NEEDED) return Recipe.PASTE;
        return Recipe.NONE;
    }

    private static int duration(Recipe r) {
        return r == Recipe.GASOLINE ? GASOLINE_TICKS : PASTE_TICKS;
    }

    // ───────────── Interacción ─────────────

    /** Mete ítems si la receta actual los admite. 'all' = intentar meter todo el stack. */
    public boolean tryInsert(ItemStack stack, boolean all, boolean consume) {
        if (!output.isEmpty() || stack.isEmpty()) return false;
        int room;
        if (waterStage()) {
            if (isCoal(stack)) room = COAL_NEEDED - count(IndustrialCauldronBlockEntity::isCoal);
            else if (isBlaze(stack)) room = BLAZE_NEEDED - count(IndustrialCauldronBlockEntity::isBlaze);
            else return false;
        } else if (gasolineStage()) {
            if (!isLeaf(stack)) return false;
            room = LEAVES_NEEDED - count(IndustrialCauldronBlockEntity::isLeaf);
        } else {
            return false;
        }
        if (room <= 0) return false;
        int amount = Math.min(room, all ? stack.getCount() : 1);

        boolean merged = false;
        for (ItemStack s : ingredients) {
            if (ItemStack.isSameItemSameComponents(s, stack)) { s.grow(amount); merged = true; break; }
        }
        if (!merged) ingredients.add(stack.copyWithCount(amount));
        if (consume) stack.shrink(amount);
        progress = 0;
        sync();
        return true;
    }

    public ItemStack removeLastIngredient() {
        if (ingredients.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = ingredients.remove(ingredients.size() - 1);
        progress = 0;
        sync();
        return taken;
    }

    public ItemStack takeOutput() {
        if (output.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = output;
        output = ItemStack.EMPTY;
        sync();
        return taken;
    }

    /** Tira el líquido (mano vacía + agachado, sin ingredientes). */
    public void discardLiquid() {
        waterMb = 0;
        gasolineMb = 0;
        progress = 0;
        sync();
    }

    public List<ItemStack> removeAllContents() {
        List<ItemStack> all = new ArrayList<>(ingredients);
        if (!output.isEmpty()) all.add(output);
        return all;
    }

    // ───────────── Fluidos (cubos y tuberías) ─────────────

    public int fillInternal(FluidStack res, boolean execute) {
        if (res.isEmpty()) return 0;
        int accepted;
        if (res.is(Fluids.WATER)) {
            if (gasolineMb > 0 || !output.isEmpty()) return 0;
            accepted = Math.min(res.getAmount(), WATER_NEEDED - waterMb);
            if (accepted > 0 && execute) waterMb += accepted;
        } else if (res.is(SOLVENT_FLUIDS)) {
            if (waterMb > 0) return 0;
            accepted = Math.min(res.getAmount(), GASOLINE_MAX - gasolineMb);
            if (accepted > 0 && execute) gasolineMb += accepted;
        } else {
            return 0;
        }
        if (accepted > 0 && execute) { progress = 0; sync(); }
        return Math.max(accepted, 0);
    }

    /** Capability de fluidos: solo admite (agua y disolventes). No se puede sacar por tubería. */
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) {
            return waterMb > 0 ? new FluidStack(Fluids.WATER, waterMb) : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) { return GASOLINE_MAX; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return stack.is(Fluids.WATER) || stack.is(SOLVENT_FLUIDS);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return fillInternal(resource, action.execute());
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    public IFluidHandler getFluidHandler() { return fluidHandler; }

    // ───────────── Calor ─────────────
    private int computeHeat() {
        if (level == null) return 0;
        BlockState below = level.getBlockState(worldPosition.below());
        if (below.is(BlockTags.CAMPFIRES)) {
            return below.hasProperty(CampfireBlock.LIT) && below.getValue(CampfireBlock.LIT) ? 1 : 0;
        }
        if (below.is(BlockTags.FIRE) || below.getFluidState().is(FluidTags.LAVA)) return 1;

        // Create: Blaze Burner (sin dependencia directa). Potenciado = doble velocidad
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(below.getBlock());
        if (key.getNamespace().equals("create") && key.getPath().equals("blaze_burner")) {
            for (Property<?> p : below.getProperties()) {
                if (p.getName().equals("blaze")) {
                    String v = valueName(below, p);
                    if (v.equals("none")) return 0;
                    if (v.equals("smouldering")) return 1;
                    return 2;
                }
            }
        }
        return 0;
    }

    private static <T extends Comparable<T>> String valueName(BlockState s, Property<T> p) {
        return p.getName(s.getValue(p)).toLowerCase(Locale.ROOT);
    }

    // ───────────── Tick (solo servidor) ─────────────
    public static void tick(Level level, BlockPos pos, BlockState state, IndustrialCauldronBlockEntity be) {
        if (level.isClientSide) return;

        Recipe r = be.currentRecipe();
        if (r == Recipe.NONE) {
            if (be.progress != 0 || be.brewing) {
                be.progress = 0;
                be.brewing = false;
                be.sync();
            }
            return;
        }

        if (level.getGameTime() % 10 == 0) be.heat = be.computeHeat();

        boolean nowBrewing = be.heat > 0;
        if (nowBrewing) be.progress += be.heat;
        if (nowBrewing != be.brewing) {
            be.brewing = nowBrewing;
            be.sync();
        }
        if (be.progress >= duration(r)) be.finish(r);
        else if (nowBrewing && level.getGameTime() % 40 == 0) be.setChanged();
    }

    private void finish(Recipe r) {
        if (r == Recipe.GASOLINE) {
            ingredients.clear();
            waterMb = 0;
            gasolineMb = Math.min(GASOLINE_MAX, gasolineMb + GASOLINE_YIELD);
        } else if (r == Recipe.PASTE) {
            int total = 0, n = 0;
            for (ItemStack s : ingredients) {
                if (isLeaf(s)) {
                    total += s.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2) * s.getCount();
                    n += s.getCount();
                }
            }
            int quality = n == 0 ? 2 : Math.round((float) total / n);
            ItemStack paste = new ItemStack(ModItems.COCA_PASTE.get(), PASTE_YIELD);
            paste.set(ModDataComponentTypes.QUALITY.get(), quality);
            ingredients.clear();
            gasolineMb -= PASTE_GASOLINE_COST;
            output = paste;
        }
        progress = 0;
        brewing = false;
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ───────────── NBT / sincronización ─────────────
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Water", waterMb);
        tag.putInt("Gasoline", gasolineMb);
        tag.putInt("Progress", progress);
        tag.putBoolean("Brewing", brewing);
        ListTag list = new ListTag();
        for (ItemStack s : ingredients) list.add(s.save(registries));
        tag.put("Ingredients", list);
        if (!output.isEmpty()) tag.put("Output", output.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        waterMb = tag.getInt("Water");
        gasolineMb = tag.getInt("Gasoline");
        progress = tag.getInt("Progress");
        brewing = tag.getBoolean("Brewing");
        ingredients.clear();
        ListTag list = tag.getList("Ingredients", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = ItemStack.parseOptional(registries, list.getCompound(i));
            if (!s.isEmpty()) ingredients.add(s);
        }
        output = tag.contains("Output") ? ItemStack.parseOptional(registries, tag.getCompound("Output")) : ItemStack.EMPTY;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
