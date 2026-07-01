package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PipeItem extends Item implements ITobaccoProduct, ISmokableItem {

    private IPipable charge;
    private final PipeType type;

    public PipeItem(Properties properties, PipeType type) {
        super(properties.component(ModDataComponentTypes.QUALITY.get(), 1).component(ModDataComponentTypes.PIPE_TYPE.get(), type));
        this.type = type;
    }

    // 🛠️ GESTIÓN DE ACCIONES (Fumar o Cargar)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipeStack = player.getItemInHand(hand);

        // 1. Si la pipa YA está cargada, el jugador fuma
        if (this.isLoaded(pipeStack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(pipeStack);
        }

        // 2. Si está vacía, intentamos cargarla con la otra mano
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        if (otherStack.getItem() instanceof IPipable) {
            // 🚨 SOLUCCIÓN: Modificamos los stacks en AMBOS lados (Cliente y Servidor)
            // Esto permite que la predicción del cliente sepa al instante que la pipa se cargó
            this.loadPipe(pipeStack, otherStack);

            // Consumimos 1 unidad del tabaco (a menos que esté en creativo)
            if (!player.getAbilities().instabuild) {
                otherStack.shrink(1);
            }

            // Pasamos el 'player' en el primer parámetro.
            // El servidor lo reproducirá para todos MENOS para el jugador, y el cliente lo reproducirá localmente.
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.2F);

            return InteractionResultHolder.sidedSuccess(pipeStack, level.isClientSide());
        }

        // Si está vacía y no tienes tabaco en la otra mano, pasamos el turno
        return InteractionResultHolder.pass(pipeStack);
    }

    // Comprobar si la pipa tiene algo dentro
    public boolean isLoaded(ItemStack pipeStack) {
        return pipeStack.has(ModDataComponentTypes.PIPE_CONTENT.get());
    }

    // Obtener lo que hay dentro
    public ItemStack getContent(ItemStack pipeStack) {
        return pipeStack.getOrDefault(ModDataComponentTypes.PIPE_CONTENT.get(), ItemStack.EMPTY);
    }

    // Cargar la pipa
    public void loadPipe(ItemStack pipeStack, ItemStack tobaccoStack) {
        // Guardamos una copia de 1 sola unidad del tabaco
        ItemStack toInsert = tobaccoStack.copyWithCount(1);
        pipeStack.set(ModDataComponentTypes.PIPE_CONTENT.get(), toInsert);
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        ItemStack contentStack = this.getContent(stack);

        // Extraemos el Item del contenido y verificamos si implementa IPipable
        if (contentStack.getItem() instanceof IPipable pipableItem) {
            // Pasamos el contentStack para que el IPipable pueda leer su propia calidad, id, etc.
            pipableItem.applyProductEffects(entity, contentStack);
        }    }

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration % 2 == 0) {
            Vec3 look = entity.getLookAngle();
            double x = entity.getX() + look.x * 0.43;
            double y = entity.getEyeY() - 0.1 + look.y * 0.1;
            double z = entity.getZ() + look.z * 0.43;

            level.addParticle(ParticleTypes.SMOKE, x, y, z, look.x * 0.1, 0.05, look.z * 0.1);

            if (getQuality(stack) >= 2) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, 0, -0.02, 0);
            }
        }
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);

            // 1. Aplicamos los efectos del tabaco que está dentro
            this.applyProductEffects(entity, stack);

            // 2. VACIAR LA PIPA (Removemos el componente para que vuelva a estar vacía)
            stack.remove(ModDataComponentTypes.PIPE_CONTENT.get());

            // 3. Desgaste de la pipa por uso
            if (entity instanceof Player player) {
                if (!player.getAbilities().instabuild) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        stack.shrink(1);
                    }
                }
            }
        }
        return stack;
    }
}
