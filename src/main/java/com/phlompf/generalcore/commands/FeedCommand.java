package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class FeedCommand {
    private FeedCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("feed");

        dispatcher.register(
                Commands.literal("feed")
                        .requires(Perms::isAllowed)
                        .executes(FeedCommand::feed)
        );
    }

    private static int feed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        FoodData fd = p.getFoodData();
        fd.setFoodLevel(20);
        fd.setSaturation(20.0F);
        fd.setExhaustion(0.0F);

        p.displayClientMessage(Component.literal("Fed to full."), true);
        return 1;
    }
}
