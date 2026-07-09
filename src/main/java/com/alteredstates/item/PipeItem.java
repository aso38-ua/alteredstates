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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Pipa de fumar. Máquina de estados: VACÍA -> CARGADA -> FUMANDO -> VACÍA.
 *
 * - VACÍA + click derecho, con un IPipable en la OTRA mano -> se carga (consume 1 del stack de esa mano).
 * - CARGADA + click derecho -> empieza a fumar (startUsingItem). Al terminar la calada, onSmokeFinished
 *   aplica los efectos del IPipable cargado, vacía la pipa y le mete 1 punto de daño.
 */
public class PipeItem extends Item implements ISmokableItem, IProduct {

    private final PipeType type;

    public PipeItem(Properties properties, PipeType type) {
        super(properties.component(ModDataComponentTypes.PIPE_TYPE.get(), type));
        this.type = type;
    }

    public PipeType getPipeType() {
        return this.type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipeStack = player.getItemInHand(hand);
        ItemStack content = getContent(pipeStack);

        // 🌿 CASO 1: pipa vacía -> intentamos cargarla con lo que haya en la otra mano
        if (content.isEmpty()) {
            InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);

            if (otherStack.getItem() instanceof IPipable) {
                if (!level.isClientSide) {
                    pipeStack.set(ModDataComponentTypes.PIPE_CONTENT.get(),
                            ItemContainerContents.fromItems(List.of(otherStack.copyWithCount(1))));
                    if (!player.isCreative()) {
                        otherStack.shrink(1);
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                return InteractionResultHolder.sidedSuccess(pipeStack, level.isClientSide);
            }

            // Nada compatible en la otra mano, no hacemos nada
            return InteractionResultHolder.pass(pipeStack);
        }

        // 💨 CASO 2: pipa cargada -> empezamos a fumar
        if (!level.isClientSide) {
            playSmokingSound(level, player, pipeStack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(pipeStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // TOOT_HORN acerca el item a la boca del jugador, visualmente es lo más parecido a fumar en pipa.
        // Si CigarItem/CigaretteItem usan otra animación, cámbiala aquí para mantener consistencia.
        return UseAnim.TOOT_HORN;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        spawnSmokeParticles(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return onSmokeFinished(stack, level, livingEntity);
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        ItemStack contentStack = this.getContent(stack);

        // Extraemos el Item del contenido y verificamos si implementa IPipable
        if (contentStack.getItem() instanceof IPipable pipableItem) {
            // Pasamos el contentStack para que el IPipable pueda leer su propia calidad, id, etc.
            pipableItem.applyProductEffects(entity, contentStack);
        }
    }

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

    // 🔍 ASUNCIÓN: IPipable expone "int getQuality(ItemStack stack)". Si en tu IPipable
    // se llama distinto (o si ISmokableItem no declara este método), ajusta la firma/@Override.
    /*@Override
    public int getQuality(ItemStack stack) {
        ItemStack content = getContent(stack);
        if (content.getItem() instanceof IPipable pipable) {
            return pipable.getQuality(content);
        }
        return 0;
    }*/

    private ItemStack getContent(ItemStack stack){
        ItemContainerContents contents = stack.getOrDefault(ModDataComponentTypes.PIPE_CONTENT.get(), ItemContainerContents.EMPTY);
        return contents.copyOne();
    }
}
