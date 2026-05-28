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
    // Definimos el tamaño del inventario (6 huecos)
    private static final int INVENTORY_SIZE = 6;
    private final ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private final int[] dryingTimes = new int[INVENTORY_SIZE];

    // ⏱️ Tiempo de secado por ítem (100 ticks = 5 segundos para pruebas)
    public static final int DRYING_TIME = 6000;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items[i] = ItemStack.EMPTY;
            this.dryingTimes[i] = 0;
        }
    }

    // Devuelve la lista completa para que el renderizador la dibuje
    public ItemStack[] getItems() {
        return this.items;
    }

    // Intenta meter un cogollo en el primer hueco libre
    public boolean addItem(ItemStack stack) {
        // Asegúrate de que el ítem registrado en ModItems se llama exactamente INDICA_BUDS_FRESH
        if (!stack.is(ModItems.INDICA_BUDS_FRESH.get())) return false;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (this.items[i].isEmpty()) {
                // Copiamos el ítem con sus componentes de datos (calidad)
                this.items[i] = stack.copyWithCount(1);
                this.dryingTimes[i] = 0;
                setChanged();

                // Medida de seguridad extra para evitar NullPointerException
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }

                return true;
            }
        }
        return false;
    }

    // Intenta sacar el primer cogollo YA SECO que encuentre
    public ItemStack takeFinishedItem() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!this.items[i].isEmpty() && this.items[i].is(ModItems.INDICA_BUDS_DRY.get())) {
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

    // El corazón del bloque: procesa cada hueco de forma independiente
    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity blockEntity) {
        boolean changed = false;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = blockEntity.items[i];

            // Si hay un cogollo fresco, aumenta su temporizador
            if (!stack.isEmpty() && stack.is(ModItems.INDICA_BUDS_FRESH.get())) {
                blockEntity.dryingTimes[i]++;

                // Si llega al tiempo requerido, se seca
                if (blockEntity.dryingTimes[i] >= DRYING_TIME) {
                    int currentQuality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                    // Creamos el cogollo seco
                    ItemStack dryStack = new ItemStack(ModItems.INDICA_BUDS_DRY.get());
                    // Subimos la calidad a BUENA (2)
                    dryStack.set(ModDataComponentTypes.QUALITY.get(), Math.max(currentQuality, 2));

                    // Reemplazamos en el slot
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

    // --- GUARDAR Y CARGAR DATOS (NBT) ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag listTag = new ListTag();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!this.items[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);

                // 🛑 LA CLAVE DE 1.21.1: Guardar el ítem en un sub-tag llamado "Item"
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

        // Limpiamos el inventario antes de cargar
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

                    // 🛑 LA CLAVE DE 1.21.1: Leer el sub-tag "Item" correctamente
                    this.items[slot] = ItemStack.parseOptional(registries, slotTag.getCompound("Item"));
                    this.dryingTimes[slot] = slotTag.getInt("DryingTime");
                }
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}