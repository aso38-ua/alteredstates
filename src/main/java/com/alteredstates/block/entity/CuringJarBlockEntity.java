package com.alteredstates.block.entity;

import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
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

    // Tiempo para curar
    public static final int MAX_CURE_TIME = 6000;

    public CuringJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURING_JAR.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return this.storedItem;
    }

    // Intenta meter cogollos al tarro
    public boolean insertItem(ItemStack stack) {
        if (!stack.is(ModItems.INDICA_BUDS_DRY.get())) return false;
        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

        // 🛑 EL FIX: Permitimos la entrada de cualquier cogollo seco (0 a 4) para Curado Artesanal [cite: 60]
        if (quality > 4) return false;

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

    // Saca el producto del tarro
    public ItemStack extractItem() {
        if (this.storedItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = this.storedItem;
        this.storedItem = ItemStack.EMPTY;
        this.curingProgress = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return taken;
    }

    // El reloj que mejora la calidad a PREMIUM (3)
    public static void tick(Level level, BlockPos pos, BlockState state, CuringJarBlockEntity blockEntity) {
        if (level.isClientSide) return;

        ItemStack stack = blockEntity.storedItem;
        if (!stack.isEmpty() && stack.is(ModItems.INDICA_BUDS_DRY.get())) {
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            // 🛑 EL FIX: El tarro solo progresa si es calidad < 4 (Premium) [cite: 61]
            if (quality < 4) {
                blockEntity.curingProgress++;

                if (blockEntity.curingProgress >= MAX_CURE_TIME) {
                    // ¡BUM! Subimos la calidad al máximo (4 = Premium en Opción B)
                    stack.set(ModDataComponentTypes.QUALITY.get(), 4);
                    blockEntity.curingProgress = 0;
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
            // Si ya es 4, las partículas salen por animateTick(...) pero el tiempo no corre
        }
    }

    // --- GUARDADO NBT DE SEGURIDAD (Fijado para 1.21.1) ---
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
            // 📩 El cliente recibe el paquete del servidor y lee los ítems guardados
            this.loadAdditional(tag, registries);

            // Forzamos al renderizador del cliente a redibujar el bloque ahora que tiene los datos
            if (this.level != null && this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }
}