package com.alteredstates.block;

import com.alteredstates.block.entity.RollingTrayBlockEntity;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

            // 🛠️ Shift + Clic vacío = Liar el porro
            if (player.isShiftKeyDown() && handStack.isEmpty()) {
                if (!tray.getPaper().isEmpty() && !tray.getWeed().isEmpty()) {

                    // Detectamos qué tipo de marihuana picada hay en la bandeja
                    boolean isIndica = tray.getWeed().is(ModItems.INDICA_GROUND.get());

                    // 🟢 CORRECCIÓN: Usamos los dos porros específicos registrados en ModItems
                    net.minecraft.world.item.Item jointResult = isIndica ? ModItems.INDICA_JOINT.get() : ModItems.SATIVA_JOINT.get();

                    ItemStack joint = new ItemStack(jointResult);

                    // Heredamos la calidad del cogollo molido
                    int weedQuality = tray.getWeed().getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
                    joint.set(ModDataComponentTypes.QUALITY.get(), weedQuality);

                    if (!player.getInventory().add(joint)) {
                        player.drop(joint, false);
                    }

                    tray.clearTray();
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
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
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getAdditive());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getWeed());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), tray.getPaper());
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
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}