package com.alteredstates.block;

import com.alteredstates.block.entity.CuringJarBlockEntity;
import com.alteredstates.registry.ModBlockEntities;
import com.alteredstates.registry.ModDataComponentTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CuringJarBlock extends BaseEntityBlock {
    // Hitbox pro: ajustada para el nuevo modelo Mason Jar detallado (X4 Z4 a X12 Z12, altura Y14)
    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 14.0D, 12.0D);

    public CuringJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Obligatorio para que use el JSON
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // 🧠 Esto es lo que vincula el bloque físico con su "cerebro" lógico
        return new CuringJarBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CuringJarBlockEntity blockEntity) {
            ItemStack itemInHand = player.getMainHandItem();

            if (!itemInHand.isEmpty()) {
                // Intentamos meter lo que hay en la mano
                if (blockEntity.insertItem(itemInHand)) {
                    level.playSound(player, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                // Mano vacía -> Sacamos los cogollos curados
                ItemStack extracted = blockEntity.extractItem();
                if (!extracted.isEmpty()) {
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof CuringJarBlockEntity blockEntity) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getStoredItem());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // IMPORTANTE: level.isClientSide ? null es correcto, el curado pasa en el servidor.
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CURING_JAR.get(), CuringJarBlockEntity::tick);
    }

    // 💨 🌸 SIMULACIÓN DE OLOR Y CALIDAD VISUAL (1.21.1)
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Solo spawneamos partículas si hay algo dentro y el random lo decide
        if (random.nextInt(3) == 0 && level.getBlockEntity(pos) instanceof CuringJarBlockEntity blockEntity) {
            ItemStack storedStack = blockEntity.getStoredItem();
            if (storedStack.isEmpty()) return;

            // Leemos la calidad actual (conservada al sincronizar el servidor con el cliente)
            int quality = storedStack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            // 🎨 Definimos los colores (RGB Float 0-1 range)
            float r, g, b;
            switch (quality) {
                case 0 -> { // Trash: Gris sucio
                    r = 0.4f; g = 0.4f; b = 0.4f;
                }
                case 1 -> { // Regular: Blanco/crema
                    r = 1.0f; g = 1.0f; b = 0.95f;
                }
                case 2 -> { // Good: Verde intenso
                    r = 0.1f; g = 0.9f; b = 0.1f;
                }
                case 3 -> { // Premium: Dorado brillante
                    r = 1.0f; g = 0.85f; b = 0.0f;
                }
                default -> { // Fallback: Blanco
                    r = 1.0f; g = 1.0f; b = 1.0f;
                }
            }

            // Calculamos la posición exacta sobre la tapa de madera
            double centerX = pos.getX() + 0.5D;
            double topY = pos.getY() + 0.875D; // Justo en la tapa de madera (14/16 D de altura)
            double centerZ = pos.getZ() + 0.5D;

            // Spawneamos partículas ENTITY_EFFECT usando ColorParticleOption (sintaxis 1.21)
            // Les damos una velocidad vertical muy lenta para que floten
            level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b), centerX, topY, centerZ, 0.0D, 0.01D, 0.0D);
        }
    }
}