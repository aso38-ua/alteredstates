package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
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

    public static final int DRYING_TIME = 6000; // Ajusta según tus necesidades

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items[i] = ItemStack.EMPTY;
            this.dryingTimes[i] = 0;
        }
    }

    public ItemStack[] getItems() { return this.items; }

    public boolean addItem(ItemStack stack) {
        // Acepta fresco de Indica o de Sativa
        if (!stack.is(ModItems.INDICA_BUDS_FRESH.get()) && !stack.is(ModItems.SATIVA_BUDS_FRESH.get())) return false;

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
            // Permite recoger seco de Indica o Sativa
            if (!this.items[i].isEmpty() && (this.items[i].is(ModItems.INDICA_BUDS_DRY.get()) || this.items[i].is(ModItems.SATIVA_BUDS_DRY.get()))) {
                ItemStack taken = this.items[i];
                this.items[i] = ItemStack.EMPTY;
                this.dryingTimes[i] = 0;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

            // Acepta fresco de Indica o de Sativa
            if (!stack.isEmpty() && (stack.is(ModItems.INDICA_BUDS_FRESH.get()) || stack.is(ModItems.SATIVA_BUDS_FRESH.get()))) {
                int currentQuality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                if (currentQuality == 0) continue;

                // 🛑 Determina cuál es el ítem seco de salida según la entrada
                net.minecraft.world.item.Item dryItemType = stack.is(ModItems.INDICA_BUDS_FRESH.get()) ? ModItems.INDICA_BUDS_DRY.get() : ModItems.SATIVA_BUDS_DRY.get();

                if (envFails) {
                    ItemStack ruinedStack = new ItemStack(dryItemType);
                    ruinedStack.set(ModDataComponentTypes.QUALITY.get(), 0);
                    blockEntity.items[i] = ruinedStack;
                    blockEntity.dryingTimes[i] = 0;
                    changed = true;
                    continue;
                }

                blockEntity.dryingTimes[i]++;

                if (blockEntity.dryingTimes[i] >= DRYING_TIME) {
                    ItemStack dryStack = new ItemStack(dryItemType);
                    dryStack.set(ModDataComponentTypes.QUALITY.get(), Math.min(4, currentQuality));

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