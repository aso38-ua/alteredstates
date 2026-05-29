package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items; // 🟢 Importamos los ítems vanilla
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RollingTrayBlockEntity extends BlockEntity {
    private ItemStack paper = ItemStack.EMPTY;
    private ItemStack weed = ItemStack.EMPTY;
    private ItemStack additive = ItemStack.EMPTY;

    public RollingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_TRAY.get(), pos, state);
    }

    public ItemStack getPaper() { return paper; }
    public ItemStack getWeed() { return weed; }
    public ItemStack getAdditive() { return additive; }

    public boolean addItem(ItemStack stack) {
        // 🟢 CAMBIO: Ahora valida usando el papel normal de Minecraft (Items.PAPER)
        if (paper.isEmpty() && stack.is(Items.PAPER)) {
            paper = stack.copyWithCount(1);
            sync();
            return true;
        }
        // Si hay papel, pero no hierba (Acepta Indica o Sativa triturada)
        else if (!paper.isEmpty() && weed.isEmpty() && (stack.is(ModItems.INDICA_GROUND.get()) || stack.is(ModItems.SATIVA_GROUND.get()))) {
            weed = stack.copyWithCount(1);
            sync();
            return true;
        }
        // Si hay papel e hierba, pero no aditivo
        else if (!paper.isEmpty() && !weed.isEmpty() && additive.isEmpty() /* && stack.is(ModTags.Items.ADDITIVES) */) {
            additive = stack.copyWithCount(1);
            sync();
            return true;
        }
        return false;
    }

    public void clearTray() {
        this.paper = ItemStack.EMPTY;
        this.weed = ItemStack.EMPTY;
        this.additive = ItemStack.EMPTY;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!paper.isEmpty()) tag.put("Paper", paper.saveOptional(registries));
        if (!weed.isEmpty()) tag.put("Weed", weed.saveOptional(registries));
        if (!additive.isEmpty()) tag.put("Additive", additive.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        paper = tag.contains("Paper") ? ItemStack.parseOptional(registries, tag.getCompound("Paper")) : ItemStack.EMPTY;
        weed = tag.contains("Weed") ? ItemStack.parseOptional(registries, tag.getCompound("Weed")) : ItemStack.EMPTY;
        additive = tag.contains("Additive") ? ItemStack.parseOptional(registries, tag.getCompound("Additive")) : ItemStack.EMPTY;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries); return tag;
    }
}