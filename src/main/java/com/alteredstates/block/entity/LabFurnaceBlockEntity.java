package com.alteredstates.block.entity;

import com.alteredstates.block.LabFurnaceBlock;
import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import com.alteredstates.util.HeatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Horno de laboratorio (sin GUI): pasta de coca -> cocaína en roca.
 *  - 600 ticks por unidad (3x más lento que un horno normal).
 *  - Fuente de calor debajo (hoguera, fuego, lava, Blaze Burner): sin gastar combustible.
 *    Blaze Burner potenciado = el doble de rápido (velocidad de horno normal).
 *  - Sin calor debajo: gasta combustible metido a mano (carbón, madera...).
 */
public class LabFurnaceBlockEntity extends BlockEntity {
    public static final int COOK_TICKS = 600;
    public static final int MAX_INPUT = 16;
    public static final int MAX_FUEL_TICKS = 12000;

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int progress = 0;
    private int fuelTicks = 0;
    private int heat = 0;

    public LabFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAB_FURNACE.get(), pos, state);
    }

    public ItemStack getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public boolean isEmpty() { return input.isEmpty() && output.isEmpty(); }

    // ───────────── Interacción ─────────────
    public boolean tryInsertInput(ItemStack stack, boolean all, boolean consume) {
        if (!stack.is(ModItems.COCA_PASTE.get())) return false;
        int room = MAX_INPUT - input.getCount();
        if (room <= 0) return false;
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, stack)) return false;
        int amount = Math.min(room, all ? stack.getCount() : 1);
        if (input.isEmpty()) input = stack.copyWithCount(amount);
        else input.grow(amount);
        if (consume) stack.shrink(amount);
        sync();
        return true;
    }

    public boolean canTakeFuel(int burnTime) {
        return burnTime > 0 && fuelTicks + burnTime <= MAX_FUEL_TICKS;
    }

    public void addFuel(int burnTime) {
        fuelTicks += burnTime;
        sync();
    }

    public ItemStack takeOutput() {
        if (output.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = output;
        output = ItemStack.EMPTY;
        sync();
        return taken;
    }

    public ItemStack takeInput() {
        if (input.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = input;
        input = ItemStack.EMPTY;
        progress = 0;
        sync();
        return taken;
    }

    // ───────────── Cocción ─────────────
    private ItemStack makeRock() {
        ItemStack rock = new ItemStack(ModItems.COCAINE_ROCK.get(), 1);
        rock.set(ModDataComponentTypes.QUALITY.get(), input.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2));
        return rock;
    }

    private boolean canCook() {
        if (input.isEmpty() || !input.is(ModItems.COCA_PASTE.get())) return false;
        if (output.isEmpty()) return true;
        return output.getCount() < output.getMaxStackSize()
                && ItemStack.isSameItemSameComponents(output, makeRock());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabFurnaceBlockEntity be) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 10 == 0) be.heat = HeatHelper.getHeatBelow(level, pos);

        int speed = 0;
        if (be.canCook()) {
            if (be.heat > 0) speed = be.heat;
            else if (be.fuelTicks > 0) { speed = 1; be.fuelTicks--; }
        } else if (be.progress != 0) {
            be.progress = 0;
            be.sync();
        }

        boolean lit = speed > 0;
        if (lit) {
            be.progress += speed;
            if (be.progress >= COOK_TICKS) be.cook();
            else if (level.getGameTime() % 40 == 0) be.setChanged();
        }
        if (state.getValue(LabFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(LabFurnaceBlock.LIT, lit), 3);
        }
    }

    private void cook() {
        ItemStack rock = makeRock();
        if (output.isEmpty()) output = rock;
        else output.grow(1);
        input.shrink(1);
        if (input.isEmpty()) input = ItemStack.EMPTY;
        progress = 0;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ───────────── NBT ─────────────
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!input.isEmpty()) tag.put("Input", input.save(registries));
        if (!output.isEmpty()) tag.put("Output", output.save(registries));
        tag.putInt("Progress", progress);
        tag.putInt("Fuel", fuelTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        input = tag.contains("Input") ? ItemStack.parseOptional(registries, tag.getCompound("Input")) : ItemStack.EMPTY;
        output = tag.contains("Output") ? ItemStack.parseOptional(registries, tag.getCompound("Output")) : ItemStack.EMPTY;
        progress = tag.getInt("Progress");
        fuelTicks = tag.getInt("Fuel");
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
