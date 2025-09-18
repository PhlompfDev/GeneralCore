package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class InvSeeCommand {
    private InvSeeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Hide from console logs
        registerSuppressedCommand("invsee");

        dispatcher.register(
                Commands.literal("invsee")
                        .requires(Perms::isAllowed)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(InvSeeCommand::openInventory))
        );
    }

    private static int openInventory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer viewer = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

        // Top inventory: live proxy to target's items/armor/offhand (54 slots)
        TargetInventoryContainer proxy = new TargetInventoryContainer(target);

        // Open vanilla chest menu (6 rows). Bottom area is viewer's own inventory+hotbar.
        Component title = Component.literal(target.getGameProfile().getName() + "'s Inventory");
        MenuProvider provider = new SimpleMenuProvider(
                (id, playerInv, p) -> ChestMenu.sixRows(id, playerInv, proxy),
                title
        );
        viewer.openMenu(provider);

        // Private confirmation
        viewer.displayClientMessage(
                Component.literal("Editing " + target.getGameProfile().getName() + "'s inventory."), true
        );
        return 1;
    }
}