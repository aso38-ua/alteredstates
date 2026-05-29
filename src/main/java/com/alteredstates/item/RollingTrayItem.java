package com.alteredstates.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import java.util.List;

public class RollingTrayItem extends BlockItem {

    public RollingTrayItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.alteredstates.rolling_tray.title").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.alteredstates.rolling_tray.step1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.alteredstates.rolling_tray.step2").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.alteredstates.rolling_tray.step3").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.alteredstates.rolling_tray.step4").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.alteredstates.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}