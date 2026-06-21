package com.alteredstates.item;

import com.alteredstates.client.ModItemProperties;
import com.alteredstates.component.CigarData;
import com.alteredstates.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class CigaretteItem extends Item implements ISmokableItem, ITobaccoProduct{

    private static final int MAX_PUFFS = 4;
    private static final long AUTO_EXTINGUISH_TICKS = 6000L; // 5 minutos

    public CigaretteItem(Properties properties) {
        super(properties.stacksTo(1).durability(MAX_PUFFS)
                .component(ModDataComponentTypes.QUALITY.get(), 1)
                .component(ModDataComponentTypes.CIGAR_DATA.get(), new CigarData(false, 0, 0L)));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.getSmokeAnimation(stack);
    }

    private CigarData getData(ItemStack stack) {
        CigarData data = stack.get(ModDataComponentTypes.CIGAR_DATA.get());
        return data != null ? data : new CigarData(false, 0, 0L);
    }

    private void setData(ItemStack stack, CigarData data) {
        stack.set(ModDataComponentTypes.CIGAR_DATA.get(), data);
        stack.setDamageValue(data.puffsTaken());
    }

    private boolean isLighterInOtherHand(Player player, InteractionHand hand) {
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        return otherStack.getItem() instanceof FlintAndSteelItem
                || otherStack.getItem() instanceof FireChargeItem;
    }

    private void igniteCigarette(Player player, ItemStack cigaretteStack, InteractionHand cigaretteHand, Level level) {
        CigarData data = getData(cigaretteStack);
        setData(cigaretteStack, data.withLit(true, level.getGameTime()));

        InteractionHand lighterHand = cigaretteHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack lighterStack = player.getItemInHand(lighterHand);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            lighterStack.hurtAndBreak(1, serverLevel, serverPlayer, item -> {});
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private boolean shouldAutoExtinguish(CigarData data, Level level) {
        return data.lit() && (level.getGameTime() - data.lastLitTick()) >= AUTO_EXTINGUISH_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        CigarData data = getData(stack);
        if (shouldAutoExtinguish(data, level)) {
            setData(stack, data.withLit(false, data.lastLitTick()));
            return;
        }
        if (data.lit()) {
            spawnSmokeParticles(level, entity, stack, remainingUseDuration);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CigarData data = getData(stack);

        if (shouldAutoExtinguish(data, level)) {
            setData(stack, data.withLit(false, data.lastLitTick()));
            data = getData(stack);
        }

        if (!data.lit()) {
            if (isLighterInOtherHand(player, hand)) {
                igniteCigarette(player, stack, hand, level);
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.fail(stack);
        }

        return startSmoking(level, player, hand);
    }

    @Override
    public void playSmokingSound(Level level, LivingEntity entity, ItemStack stack) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public void spawnSmokeParticles(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide()) return;
        Vec3 mouthPos = entity.getEyePosition().add(entity.getLookAngle().scale(0.3));
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                mouthPos.x, mouthPos.y, mouthPos.z, 0.0, 0.02, 0.0);
    }

    @Override
    public ItemStack onSmokeFinished(ItemStack stack, Level level, LivingEntity entity) {
        CigarData data = getData(stack);
        int newPuffs = data.puffsTaken() + 1;

        if (newPuffs >= MAX_PUFFS) {
            stack.shrink(1);
            return stack;
        }

        setData(stack, data.withPuff(level.getGameTime()));
        applyProductEffects(entity, stack);
        return stack;
    }

    @Override
    public int getQuality(ItemStack stack) {
        Integer quality = stack.get(ModDataComponentTypes.QUALITY.get());
        return quality != null ? quality : 1;
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
        // Pendiente: efectos de nicotina/relajación según mecánicas del mod
    }

    public int getVisualStateIndex(ItemStack stack) {
        CigarData data = getData(stack);
        if (!data.lit() && data.puffsTaken() == 0) return 0;       // apagado, nuevo
        if (data.lit()) return 1 + data.puffsTaken();               // encendido: 1,2,3,4
        return 4 + data.puffsTaken();                                // apagado, consumido: 5,6,7
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        // Usamos el método getQuality() unificado
        int quality = getQuality(stack);

        net.minecraft.ChatFormatting color = switch (quality) {
            case 0 -> net.minecraft.ChatFormatting.GRAY;
            case 1 -> net.minecraft.ChatFormatting.WHITE;
            case 2 -> net.minecraft.ChatFormatting.AQUA;
            case 3 -> net.minecraft.ChatFormatting.GREEN;
            default -> net.minecraft.ChatFormatting.GOLD;
        };

        net.minecraft.network.chat.Component qualityName = switch (quality) {
            case 0 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.regular");
            case 2 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.normal");
            case 3 -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.good");
            default -> net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality.premium");
        };

        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.quality_format", qualityName)
                .withStyle(color));

        int remainingSmokes = stack.getMaxDamage() - stack.getDamageValue();
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("tooltip.alteredstates.remaining_smokes", remainingSmokes)
                .withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        ModItemProperties.register(event);
    }
}