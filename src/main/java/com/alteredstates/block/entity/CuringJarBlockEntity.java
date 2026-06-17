package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CuringJarBlockEntity extends BlockEntity {
    private ItemStack storedItem = ItemStack.EMPTY;
    private int curingProgress = 0;

    public CuringJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURING_JAR.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return this.storedItem;
    }

    // 📥 Intenta meter productos al tarro
    public boolean insertItem(ItemStack stack) {
        // Magia: Acepta cualquier cosa que implemente ICurable
        if (!(stack.getItem() instanceof com.alteredstates.item.ICurable)) return false;

        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

        // Si ya es calidad 4 (Premium) o superior, no tiene sentido curarlo
        if (quality >= 4) return false;

        if (this.storedItem.isEmpty()) {
            this.storedItem = stack.copyWithCount(stack.getCount());
            stack.setCount(0);
        } else if (ItemStack.isSameItemSameComponents(this.storedItem, stack)) {
            int room = this.storedItem.getMaxStackSize() - this.storedItem.getCount();
            int toMove = Math.min(room, stack.getCount());
            this.storedItem.grow(toMove);
            stack.shrink(toMove);
        } else {
            return false;
        }

        this.curingProgress = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    // 📤 Saca el producto del tarro
    public ItemStack extractItem() {
        if (this.storedItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = this.storedItem;
        this.storedItem = ItemStack.EMPTY;
        this.curingProgress = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return taken;
    }

    // ⏳ El reloj que mejora la calidad a PREMIUM (4)
    public static void tick(Level level, BlockPos pos, BlockState state, CuringJarBlockEntity blockEntity) {
        if (level.isClientSide) return;

        ItemStack stack = blockEntity.storedItem;

        // Verificamos si el ítem es curable
        if (!stack.isEmpty() && stack.getItem() instanceof com.alteredstates.item.ICurable curable) {
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            if (quality < 4) {
                blockEntity.curingProgress++;

                // Le preguntamos al ítem cuánto tarda en curarse
                int targetCuringTime = curable.getCuringTime(stack);

                if (blockEntity.curingProgress >= targetCuringTime) {
                    stack.set(ModDataComponentTypes.QUALITY.get(), 4);
                    blockEntity.curingProgress = 0;
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    // --- GUARDADO NBT DE SEGURIDAD ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.storedItem.isEmpty()) {
            tag.put("StoredItem", this.storedItem.saveOptional(registries));
        }
        tag.putInt("CuringProgress", this.curingProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredItem")) {
            this.storedItem = ItemStack.parseOptional(registries, tag.getCompound("StoredItem"));
        } else {
            this.storedItem = ItemStack.EMPTY;
        }
        this.curingProgress = tag.getInt("CuringProgress");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        net.minecraft.nbt.CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.loadAdditional(tag, registries);
            if (this.level != null && this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }
}