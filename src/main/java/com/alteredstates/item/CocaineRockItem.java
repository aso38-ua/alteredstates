package com.alteredstates.item;

import com.alteredstates.registry.ModDataComponentTypes;
import com.alteredstates.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Roca de cocaína: el grinder la convierte en polvo (conserva la calidad). */
public class CocaineRockItem extends Item implements IGrindable {

    public CocaineRockItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getGrindResult(ItemStack stack) {
        return ModItems.COCAINE_POWDER.get();
    }

    @Override
    public void applyProductEffects(LivingEntity entity, ItemStack stack) {
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int quality = stack.getOrDefault(ModDataComponentTypes.QUALITY.get(), 2);

        ChatFormatting color = switch (quality) {
            case 0 -> ChatFormatting.GRAY;
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.GREEN;
            default -> ChatFormatting.GOLD;
        };

        Component qualityName = switch (quality) {
            case 0 -> Component.translatable("tooltip.alteredstates.quality.bad");
            case 1 -> Component.translatable("tooltip.alteredstates.quality.regular");
            case 2 -> Component.translatable("tooltip.alteredstates.quality.normal");
            case 3 -> Component.translatable("tooltip.alteredstates.quality.good");
            default -> Component.translatable("tooltip.alteredstates.quality.premium");
        };

        tooltip.add(Component.translatable("tooltip.alteredstates.quality_format", qualityName).withStyle(color));
        tooltip.add(Component.translatable("tooltip.alteredstates.cocaine_rock.grind_hint").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.alteredstates.cocaine.warning").withStyle(ChatFormatting.RED));
    }
}
