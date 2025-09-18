package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class HealCommand {
    private HealCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("heal");

        dispatcher.register(
                Commands.literal("heal")
                        .requires(Perms::isAllowed)
                        .executes(HealCommand::heal)
        );
    }

    private static int heal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();

        // Full health, clear fire, full air. (Does not touch invisibility/vanish.)
        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setAirSupply(p.getMaxAirSupply());

        // Optional: remove all effects. Comment this out if you want to keep buffs.
        p.removeAllEffects();

        p.displayClientMessage(Component.literal("Healed."), true);
        return 1;
    }
}
