// ===============================================
// Uses ProtectionManager
// ===============================================
package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class GodModeCommand {
    private GodModeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("god");

        dispatcher.register(
                Commands.literal("god")
                        .requires(Perms::isAllowed)
                        .executes(GodModeCommand::toggleGod)
        );
    }

    private static int toggleGod(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer p = context.getSource().getPlayerOrException();

        boolean newState = !ProtectionManager.isGod(p);
        ProtectionManager.setGod(p, newState);
        ProtectionManager.applyAbilities(p);

        p.displayClientMessage(Component.literal("God mode " + (newState ? "enabled" : "disabled")), true);
        return 1;
    }
}
