package com.alteredstates.block;

import com.alteredstates.block.entity.RollingTrayBlockEntity;
import com.alteredstates.item.*;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.Containers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class RollingTrayBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    public RollingTrayBlock(Properties properties) { super(properties); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return null; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RollingTrayBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RollingTrayBlockEntity tray) {
            ItemStack handStack = player.getMainHandItem();

            // 🛠️ Shift + Clic vacío = Liar el producto (Porro de hierba, cigarro de tabaco, etc.)
            if (player.isShiftKeyDown() && handStack.isEmpty()) {
                if (!tray.getPaper().isEmpty() && !tray.getWeed().isEmpty()) {

                    // 🟢 MAGIA: Verificamos si lo que hay en la bandeja es lialble
                    if (tray.getWeed().getItem() instanceof com.alteredstates.item.IRollable rollable) {

                        // Creamos el porro/cigarro base
                        net.minecraft.world.item.Item resultItem = rollable.getRollResult(tray.getWeed());
                        ItemStack finalProduct = new ItemStack(resultItem);

                        // Le pasamos la calidad
                        int quality = rollable.getQuality(tray.getWeed());
                        finalProduct.set(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), quality);

                        // 💉 LE INYECTAMOS QUÉ LLEVA DENTRO
                        String contentType = rollable.getContentType(tray.getWeed());
                        finalProduct.set(com.alteredstates.registry.ModDataComponentTypes.CONTENT_TYPE.get(), contentType);

                        if (!player.getInventory().add(finalProduct)) {
                            player.drop(finalProduct, false);
                        }

                        tray.clearTray();
                        level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
                // Si es un puro
                else if(!tray.getLeaves().isEmpty()){
                    //CigarType type = matchCigarRecipe(tray.getLeaves());
                    //if (type != null) {
                        // Creamos el porro/cigarro base
                    Item cigar = matchCigarRecipe(tray.getLeaves());
                    if(cigar != null) {
                        net.minecraft.world.item.Item resultItem = cigar;
                        ItemStack finalProduct = new ItemStack(resultItem);

                        // Le pasamos la calidad
                        int quality = getLeavesQuality(tray.getLeaves());
                        finalProduct.set(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), quality);

                        if (!player.getInventory().add(finalProduct)) {
                            player.drop(finalProduct, false);
                        }

                        tray.clearTray();
                        level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                        // rolar el puro correspondiente
                    }
                }
            }
            // 📥 Clic normal con ítem = Añadir a la bandeja (Papel, triturados o aditivos)
            else if (!handStack.isEmpty()) {
                if (tray.addItem(handStack)) {
                    if (!player.getAbilities().instabuild) handStack.shrink(1);
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.5F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            // 📤 Clic normal vacío = Retirar todo de la bandeja
            else if (handStack.isEmpty() && !player.isShiftKeyDown()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getAdditive());
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getWeed());
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getPaper());
                for (ItemStack leave : tray.getLeaves()) net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), leave);
                tray.clearTray();
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof RollingTrayBlockEntity tray) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getPaper());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getWeed());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getAdditive());
                for (ItemStack leave : tray.getLeaves()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), leave);

            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private Item matchCigarRecipe(ArrayList<ItemStack> leaves) {
        int tripa = 0, capa = 0, capote = 0;

        for (ItemStack stack : leaves) {
            if (!(stack.getItem() instanceof TobaccoDryLeafItem leafItem)) continue;

            TobaccoLeafType type = leafItem.getType(stack);
            if(type != null) {
                switch (type) {
                    case TRIPA -> tripa += stack.getCount();
                    case CAPA -> capa += stack.getCount();
                    case CAPOTE -> capote += stack.getCount();
                }
            }
        }

        // Comprobamos primero las recetas más restrictivas (Lancero exige premium)
        if (capote >= 2 && capa >= 1 && tripa == 0) {
            return ModItems.LANCERO_CIGAR.get();
        }
        if (tripa >= 1 && capa >= 2 && capote == 0) {
            return ModItems.TORITO_CIGAR.get();
        }
        if (tripa >= 2 && capote >= 1 && capa == 0) {
            return ModItems.ESPLENDIDO_CIGAR.get();
        }
        if (tripa >= 1 && capa >= 1 && capote >= 1) {
            return ModItems.DON_JAVIER_CIGAR.get();
        }

        return null; // combinación no válida
    }

    private int getLeavesQuality(ArrayList<ItemStack> leaves){
        int minQuality = Integer.MAX_VALUE;

        for (ItemStack stack : leaves) {
            if (!(stack.getItem() instanceof TobaccoDryLeafItem leafItem)) continue;

            int quality = leafItem.getQuality(stack);
            if (quality < minQuality) minQuality = quality;
        }
        return minQuality;
    }
}