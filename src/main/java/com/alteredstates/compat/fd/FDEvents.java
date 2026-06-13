package com.alteredstates.compat.fd;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

public class FDEvents {

    // Tag universal de cuchillos (compatible con cualquier mod de cuchillos)
    private static final TagKey<Item> KNIVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/knives"));

    @SubscribeEvent
    public static void onCuttingBoardRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        // ¿Es una tabla de cortar de Farmer's Delight?
        if (be instanceof CuttingBoardBlockEntity cuttingBoard) {
            ItemStack heldItem = event.getItemStack();

            // ¿El jugador tiene un cuchillo en la mano?
            if (heldItem.is(KNIVES)) {
                ItemStack boardItem = cuttingBoard.getStoredItem();

                if (!boardItem.isEmpty()) {
                    ItemStack resultStack = ItemStack.EMPTY;

                    // Identificamos qué cogollo está en la tabla
                    if (boardItem.is(ModItems.INDICA_BUDS_DRY.get())) {
                        resultStack = new ItemStack(ModItems.INDICA_GROUND.get());
                    } else if (boardItem.is(ModItems.SATIVA_BUDS_DRY.get())) {
                        resultStack = new ItemStack(ModItems.SATIVA_GROUND.get());
                    }

                    // Si es uno de nuestros cogollos, ejecutamos la lógica dinámica
                    if (!resultStack.isEmpty()) {
                        // 🧬 HERENCIA DE CALIDAD: Leemos la calidad del cogollo y se la inyectamos al resultado
                        int quality = boardItem.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                        resultStack.set(ModDataComponentTypes.QUALITY.get(), quality);

                        // Spawneamos el ítem resultante justo encima de la tabla
                        double x = pos.getX() + 0.5;
                        double y = pos.getY() + 0.2;
                        double z = pos.getZ() + 0.5;
                        ItemEntity entity = new ItemEntity(level, x, y, z, resultStack);
                        entity.setDefaultPickUpDelay();
                        level.addFreshEntity(entity);

                        // Desgastamos el cuchillo del jugador (si no está en creativo)
                        Player player = event.getEntity();
                        if (!player.isCreative()) {
                            heldItem.hurtAndBreak(1, player, player.getEquipmentSlotForItem(heldItem));
                        }

                        // Limpiamos la tabla de cortar y la sincronizamos con el mundo
                        boardItem.shrink(boardItem.getCount());
                        BlockState state = level.getBlockState(pos);
                        cuttingBoard.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);

                        // Efectos de sonido ambientales
                        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);

                        // Cancelamos el evento para que Farmer's Delight no intente procesar su receta estática por defecto
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
}