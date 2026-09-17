package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DryingRackBlockEntity extends BlockEntity {
    private static final int INVENTORY_SIZE = 6;
    private final ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private final int[] dryingTimes = new int[INVENTORY_SIZE];

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items[i] = ItemStack.EMPTY;
            this.dryingTimes[i] = 0;
        }
    }

    public ItemStack[] getItems() { return this.items; }

    public boolean addItem(ItemStack stack) {
        if (!(stack.getItem() instanceof com.alteredstates.item.IDryable)) return false;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (this.items[i].isEmpty()) {
                this.items[i] = stack.copyWithCount(1);
                this.dryingTimes[i] = 0;
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return true;
            }
        }
        return false;
    }

    public ItemStack takeFinishedItem() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!this.items[i].isEmpty() && !(this.items[i].getItem() instanceof com.alteredstates.item.IDryable)) {
                ItemStack taken = this.items[i];
                this.items[i] = ItemStack.EMPTY;
                this.dryingTimes[i] = 0;
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity blockEntity) {
        boolean changed = false;
        boolean envFails = level.isRainingAt(pos.above());

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = blockEntity.items[i];

            if (!stack.isEmpty() && stack.getItem() instanceof com.alteredstates.item.IDryable dryable) {
                int currentQuality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                if (currentQuality == 0) continue;

                net.minecraft.world.item.Item dryItemType = dryable.getDriedResult(stack);
                int targetDryingTime = dryable.getDryingTime(stack);

                if (envFails) {
                    ItemStack ruinedStack = new ItemStack(dryItemType);
                    ruinedStack.set(ModDataComponentTypes.QUALITY.get(), 0);
                    blockEntity.items[i] = ruinedStack;
                    blockEntity.dryingTimes[i] = 0;
                    changed = true;
                    continue;
                }

                blockEntity.dryingTimes[i]++;

                if (blockEntity.dryingTimes[i] >= targetDryingTime) {
                    ItemStack dryStack = new ItemStack(dryItemType);

                    // 1. Transferimos la calidad
                    dryStack.set(ModDataComponentTypes.QUALITY.get(), Math.min(4, currentQuality));

                    // 2. 🌿 Transferimos la genética (Cannabis) si el ítem la tiene
                    if (stack.has(ModDataComponentTypes.IS_INDICA.get())) {
                        dryStack.set(ModDataComponentTypes.IS_INDICA.get(), stack.get(ModDataComponentTypes.IS_INDICA.get()));
                    }

                    // 3. 🍂 Transferimos el tipo de hoja (Tabaco) si el ítem lo tiene
                    if (stack.has(ModDataComponentTypes.TOBACCO_LEAF_TYPE.get())) {
                        dryStack.set(ModDataComponentTypes.TOBACCO_LEAF_TYPE.get(), stack.get(ModDataComponentTypes.TOBACCO_LEAF_TYPE.get()));
                    }

                    blockEntity.items[i] = dryStack;
                    blockEntity.dryingTimes[i] = 0;
                    changed = true;
                }
            }
        }

        if (changed) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag listTag = new ListTag();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!this.items[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                slotTag.put("Item", this.items[i].saveOptional(registries));
                slotTag.putInt("DryingTime", this.dryingTimes[i]);
                listTag.add(slotTag);
            }
        }
        tag.put("Inventory", listTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items[i] = ItemStack.EMPTY;
            this.dryingTimes[i] = 0;
        }
        if (tag.contains("Inventory", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Inventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag slotTag = listTag.getCompound(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < INVENTORY_SIZE) {
                    this.items[slot] = ItemStack.parseOptional(registries, slotTag.getCompound("Item"));
                    this.dryingTimes[slot] = slotTag.getInt("DryingTime");
                }
            }
        }
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