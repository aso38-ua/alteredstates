package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class RollingTrayBlockEntity extends BlockEntity {
    private ItemStack paper = ItemStack.EMPTY;
    private ItemStack weed = ItemStack.EMPTY; //CAMBIAR NOMBRE SI JAVI SE QUEJA PORQUE PONE WEED
    private ItemStack additive = ItemStack.EMPTY; // Para el futuro (filtros, tabaco para mezclar...)
    private ArrayList<ItemStack> leaves = new ArrayList<>();

    public RollingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_TRAY.get(), pos, state);
    }

    public ItemStack getPaper() { return paper; }
    public ItemStack getWeed() { return weed; }
    public ItemStack getAdditive() { return additive; }
    public ArrayList<ItemStack> getLeaves() { return leaves; }

    public boolean addItem(ItemStack stack) {
        // 1. Añadir papel
        if (paper.isEmpty() && stack.is(Items.PAPER) && leaves.isEmpty()) {
            paper = stack.copyWithCount(1);
            stack.shrink(1);
            sync();
            return true;
        }

        //Siempre que sean hojas caerán aquí, max 3
        if(paper.isEmpty() && weed.isEmpty() && stack.getItem() instanceof com.alteredstates.item.TobaccoDryLeafItem && leaves.size()<3){
            leaves.add(stack.copyWithCount(1));
            stack.shrink(1);
            sync();
            return true;
        }


        // 2. Añadir contenido principal (CUALQUIER cosa lialble: Marihuana, Tabaco...)
        if (weed.isEmpty() && stack.getItem() instanceof com.alteredstates.item.IRollable && leaves.isEmpty()) {
            weed = stack.copyWithCount(1);
            stack.shrink(1);
            sync();
            return true;
        }


        // 3. (Futuro) Añadir aditivos si hace falta...
        return false;
    }

    public void clearTray() {
        paper = ItemStack.EMPTY;
        weed = ItemStack.EMPTY;
        leaves = new ArrayList<>();
        additive = ItemStack.EMPTY;
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
        if (!leaves.isEmpty()) for(ItemStack leave : leaves) tag.put("Leave", leave.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        paper = tag.contains("Paper") ? ItemStack.parseOptional(registries, tag.getCompound("Paper")) : ItemStack.EMPTY;
        weed = tag.contains("Weed") ? ItemStack.parseOptional(registries, tag.getCompound("Weed")) : ItemStack.EMPTY;
        additive = tag.contains("Additive") ? ItemStack.parseOptional(registries, tag.getCompound("Additive")) : ItemStack.EMPTY;
        for (ItemStack leave : leaves) leave = tag.contains("Leave") ? ItemStack.parseOptional(registries, tag.getCompound("Leave")) : ItemStack.EMPTY;
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