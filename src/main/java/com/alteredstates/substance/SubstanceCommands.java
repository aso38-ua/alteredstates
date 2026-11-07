package com.alteredstates.substance;

import com.alteredstates.AlteredStates;
import com.alteredstates.cocaine.CocaineEffects;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * /alteredstates status [jugador]            -> tolerancia, dependencia y mono de cada sustancia (cualquiera, a sí mismo)
 * /alteredstates reset <sustancia|all> [jugador] -> pone a 0 (requiere permiso de operador)
 */
@EventBusSubscriber(modid = AlteredStates.MOD_ID)
public class SubstanceCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("alteredstates")
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> status(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("reset")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("substance", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ids(), builder))
                                .executes(ctx -> reset(ctx.getSource(), ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "substance")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"),
                                                StringArgumentType.getString(ctx, "substance")))))));
    }

    private static List<String> ids() {
        List<String> ids = new ArrayList<>();
        for (ISubstance s : SubstanceManager.all()) ids.add(s.getId());
        ids.add("cocaine");
        ids.add("all");
        return ids;
    }

    private static int status(CommandSourceStack src, ServerPlayer p) {
        long now = p.level().getGameTime();
        src.sendSuccess(() -> Component.translatable("command.alteredstates.status.header", p.getDisplayName())
                .withStyle(ChatFormatting.GOLD), false);
        for (ISubstance s : SubstanceManager.all()) {
            SubstanceData data = SubstanceManager.getOrCreateData(p, s.getId());
            line(src, s.getDisplayName(), data.tolerance, data.dependency, s.hasWithdrawal(data, now));
        }
        line(src, Component.translatable("substance.alteredstates.cocaine"),
                CocaineEffects.getTolerance(p), CocaineEffects.getDependency(p), CocaineEffects.hasWithdrawal(p, now));
        src.sendSuccess(() -> Component.translatable("command.alteredstates.status.cocaine_doses",
                CocaineEffects.getDoses(p), CocaineEffects.getOverdoseRiskPercent(p), CocaineEffects.getDoseResetSeconds(p))
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static void line(CommandSourceStack src, Component name, int tolerance, int dependency, boolean withdrawal) {
        Component state = withdrawal
                ? Component.translatable("command.alteredstates.status.withdrawal").withStyle(ChatFormatting.RED)
                : Component.translatable("command.alteredstates.status.ok").withStyle(ChatFormatting.GREEN);
        src.sendSuccess(() -> Component.translatable("command.alteredstates.status.line",
                name, tolerance, dependency, state), false);
    }

    private static int reset(CommandSourceStack src, ServerPlayer p, String id) {
        if (id.equals("all")) {
            SubstanceManager.resetAll(p);
        } else if (id.equals("cocaine")) {
            CocaineEffects.reset(p);
        } else if (SubstanceManager.getSubstance(id) != null) {
            SubstanceManager.reset(p, id);
        } else {
            src.sendFailure(Component.translatable("command.alteredstates.reset.unknown", id));
            return 0;
        }
        src.sendSuccess(() -> Component.translatable("command.alteredstates.reset.done", id, p.getDisplayName()), true);
        return 1;
    }
}
