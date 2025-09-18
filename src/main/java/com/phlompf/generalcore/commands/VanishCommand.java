// ===============================================
// Invis + hide/show + AI ignore.
// ===============================================

package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class VanishCommand {
    private VanishCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("vanish");

        dispatcher.register(
                Commands.literal("vanish")
                        .requires(Perms::isAllowed)
                        .executes(VanishCommand::toggleVanish)
        );
    }

    private static int toggleVanish(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();

        boolean enabling = !ProtectionManager.isVanished(p);
        if (enabling) {
            ProtectionManager.setVanished(p, true);
            enableVanish(p);
            p.displayClientMessage(Component.literal("Vanish enabled"), true);
        } else {
            ProtectionManager.setVanished(p, false);
            disableVanish(p);
            p.displayClientMessage(Component.literal("Vanish disabled"), true);
        }
        ProtectionManager.applyAbilities(p); // recompute invul strictly from GOD

        if (ProtectionManager.isVanished(p)) {
            VanishCommand.reassertHidden(p);
        }
        return 1;
    }

    private static void enableVanish(ServerPlayer player) {
        // Hide from all online players now
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
            }
        }
        // Invisible flag syncs to client render
        player.setInvisible(true);

        // Clear any existing aggro immediately
        clearNearbyAggro(player, 32.0D);
    }

    private static void disableVanish(ServerPlayer player) {
        // Show again to everybody quickly
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(new ClientboundAddPlayerPacket(player));
            }
        }
        player.setInvisible(false);

        // Proactively make nearby mobs consider you again
        reengageNearbyMobs(player, 32.0D);
    }

    /** Null out targets & stop navigation for mobs currently targeting the player. */
    private static void clearNearbyAggro(ServerPlayer player, double range) {
        AABB box = player.getBoundingBox().inflate(range);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, box)) {
            if (!mob.isAlive() || !mob.isEffectiveAi()) continue;
            if (mob.getTarget() == player) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    /** Encourage mobs to re-target the player after unvanish. */
    private static void reengageNearbyMobs(ServerPlayer player, double range) {
        AABB box = player.getBoundingBox().inflate(range);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, box)) {
            if (!mob.isAlive() || !mob.isEffectiveAi()) continue;

            if (mob.getTarget() == player) {
                mob.setTarget(null); // clear stale target so AI can re-evaluate
            }
            // If LOS, set immediately; else normal AI will pick up soon
            if (mob.getSensing().hasLineOfSight(player)) {
                mob.setTarget(player);
            } else {
                mob.getNavigation().stop();
            }
        }
    }

    // VanishCommand.java (add this method)
    public static void reassertHidden(ServerPlayer player) {
        // Keep the flag set for client-side render checks
        if (!player.isInvisible()) player.setInvisible(true);

        // Hide from everyone again in case tracking got refreshed
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other != player) {
                other.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
            }
        }
    }

}