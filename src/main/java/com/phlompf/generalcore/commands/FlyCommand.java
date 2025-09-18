package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class FlyCommand {
    private FlyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("fly");

        dispatcher.register(
                Commands.literal("fly")
                        .requires(Perms::isAllowed)
                        .executes(FlyCommand::toggleFly)
        );
    }

    private static int toggleFly(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();

        boolean newMayfly = !p.getAbilities().mayfly;
        p.getAbilities().mayfly = newMayfly;
        if (!newMayfly && p.getAbilities().flying) {
            p.getAbilities().flying = false; // drop out of flight cleanly
        }
        p.onUpdateAbilities();

        // If you're vanished, reassert invisibility in case clients re-tracked after ability update
        if (ProtectionManager.isVanished(p)) {
            VanishCommand.reassertHidden(p);
        }

        p.displayClientMessage(Component.literal("Flight " + (newMayfly ? "enabled" : "disabled")), true);
        return 1;
    }
}
