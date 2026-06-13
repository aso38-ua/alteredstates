package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities; // ¡Recuerda registrarlo!
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BongBlockEntity extends BlockEntity {
    private ItemStack bowlContent = ItemStack.EMPTY;

    public BongBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BONG.get(), pos, state);
    }

    public ItemStack getBowlContent() { return bowlContent; }

    public void setBowlContent(ItemStack stack) {
        this.bowlContent = stack;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!bowlContent.isEmpty()) tag.put("Bowl", bowlContent.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bowlContent = tag.contains("Bowl") ? ItemStack.parseOptional(registries, tag.getCompound("Bowl")) : ItemStack.EMPTY;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries); return tag;
    }
}