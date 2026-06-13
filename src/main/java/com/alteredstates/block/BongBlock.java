package com.alteredstates.block;

import com.alteredstates.block.entity.BongBlockEntity;
import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import com.alteredstates.util.SmokingEffectProcessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BongBlock extends BaseEntityBlock {
    // 🟢 Las propiedades van DENTRO de la clase
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_WEED = BooleanProperty.create("has_weed");

    private static final VoxelShape BASE = Block.box(5, 0, 5, 11, 6, 11);
    private static final VoxelShape NECK = Block.box(6.5, 6, 6.5, 9.5, 14, 9.5);
    private static final VoxelShape SHAPE = Shapes.or(BASE, NECK);

    public BongBlock(Properties properties) {
        super(properties);
        // Registramos el estado inicial: sin agua y sin hierba
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_WATER, false).setValue(HAS_WEED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_WATER, HAS_WEED);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return null; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BongBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof BongBlockEntity bong) {
            ItemStack handStack = player.getMainHandItem();

            // 💧 1. Llenar con Cubo de Agua
            if (handStack.is(Items.WATER_BUCKET) && !state.getValue(HAS_WATER)) {
                level.setBlock(pos, state.setValue(HAS_WATER, true), 3);
                if (!player.isCreative()) player.setItemInHand(player.getUsedItemHand(), new ItemStack(Items.BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            // 🌿 2. Cargar la cazoleta con hierba picada
            if (handStack.is(ModItems.INDICA_GROUND.get()) || handStack.is(ModItems.SATIVA_GROUND.get())) {
                if (bong.getBowlContent().isEmpty()) {
                    bong.setBowlContent(handStack.copyWithCount(1));
                    // 🟢 Le decimos al bloque que ahora tiene hierba
                    level.setBlock(pos, state.setValue(HAS_WEED, true), 3);
                    if (!player.isCreative()) handStack.shrink(1);
                    level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // 💨 3. FUMAR (Mechero en mano + Tiene agua + Tiene hierba)
            if (handStack.is(Items.FLINT_AND_STEEL) && state.getValue(HAS_WATER) && !bong.getBowlContent().isEmpty()) {

                // 🛠️ Guardamos una copia de los datos de la hierba ANTES de borrarla
                ItemStack weed = bong.getBowlContent().copy();
                boolean isIndica = weed.is(ModItems.INDICA_GROUND.get());
                int quality = weed.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.5F, 1.2F);
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                    SmokingEffectProcessor.applyBongEffects(player, isIndica, quality);
                    handStack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(player.getUsedItemHand()));
                } else {
                    // Partículas en el lado del cliente
                    for (int i = 0; i < 15; i++) {
                        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                (level.random.nextDouble() - 0.5) * 0.1, 0.1, (level.random.nextDouble() - 0.5) * 0.1);
                    }
                }

                // 🛠️ FIX: Vaciamos la cazoleta FUERA del if, para que ocurra en el Cliente y en el Servidor a la vez
                bong.setBowlContent(ItemStack.EMPTY);
                level.setBlock(pos, state.setValue(HAS_WEED, false), 3);

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof BongBlockEntity bong) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bong.getBowlContent());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}