package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class JointItem extends Item implements ISmokableItem {

    private final boolean isIndica;

    public JointItem(Properties properties, boolean isIndica) {
        // MaxDamage(4) significa que el porro tiene exactamente 4 caladas antes de romperse
        super(properties.stacksTo(1).durability(4));
        this.isIndica = isIndica;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        playSmokingSound(level, player, player.getItemInHand(hand));
        return this.startSmoking(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.getSmokeAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.getSmokeDuration(stack);
    }

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        // Sonido de chisporroteo (el del fuego es ideal para simular el papel quemándose)
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        this.spawnSmokeParticles(level, entity, stack, remainingUseDuration);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // Solo spawneamos partículas cada 2 ticks para no colapsar la pantalla de humo
        if (level.isClientSide && remainingUseDuration % 2 == 0) {
            Vec3 look = entity.getLookAngle();
            // Calculamos la posición de la boca del jugador aproximadamente
            double x = entity.getX() + look.x * 0.43;
            double y = entity.getEyeY() - 0.1 + look.y * 0.1;
            double z = entity.getZ() + look.z * 0.43;

            // Humo normal de Minecraft saliendo de la punta del porro
            level.addParticle(ParticleTypes.SMOKE, x, y, z, look.x * 0.1, 0.05, look.z * 0.1);

            // Si es de buena calidad, ¡añadimos unas chispitas extra de herboristería!
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);
            if (quality >= 2) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, 0, -0.02, 0);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return this.onSmokeFinished(stack, level, entity);
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            // Sonido de exhalar el humo al terminar la calada
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);

            // Obtener calidad para el futuro multiplicador de efectos
            int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 1);

            com.alteredstates.util.SmokingEffectProcessor.applySmokingEffects(entity, this.isIndica, quality);

            // Reducimos la durabilidad del porro en 1 calada
            if (entity instanceof Player player) {
                if (!player.getAbilities().instabuild) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    // If durabilidad llega al máximo, el porro se consume entero
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        stack.shrink(1);
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        // Leemos la calidad del componente de datos (por defecto 1)
        int quality = stack.getOrDefault(com.alteredstates.registry.ModDataComponentTypes.QUALITY.get(), 1);

        // 🟢 1. Definimos el color según tu escala
        net.minecraft.ChatFormatting color = switch (quality) {
            case 0 -> net.minecraft.ChatFormatting.GRAY;
            case 1 -> net.minecraft.ChatFormatting.WHITE;
            case 2 -> net.minecraft.ChatFormatting.AQUA;
            case 3 -> net.minecraft.ChatFormatting.GREEN;
            default -> net.minecraft.ChatFormatting.GOLD;
        };

        // 🟢 2. Asignamos una clave de traducción con nombre propio según el número
        net.minecraft.network.chat.Component qualityName = switch (quality) {
            case 0 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.normal");
            case 2 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.regular");
            case 3 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.good");
            default -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.premium");
        };

        // 🟢 3. Inyectamos el nombre legible dentro del formato principal "Calidad: %s"
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality_format", qualityName)
                .withStyle(color));

        // Chivato de cuántas caladas le quedan basados en la durabilidad actual
        int remainingSmokes = stack.getMaxDamage() - stack.getDamageValue();
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.remaining_smokes", remainingSmokes)
                .withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
    }
}