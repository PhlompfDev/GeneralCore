package com.phlompf.generalcore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import static com.phlompf.generalcore.GeneralCore.registerSuppressedCommand;

public final class MobIgnoreCommand {
    private MobIgnoreCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSuppressedCommand("mobignore");

        dispatcher.register(
                Commands.literal("mobignore")
                        .requires(Perms::isAllowed)
                        .executes(MobIgnoreCommand::toggle)
        );
    }

    private static int toggle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        boolean enabling = !ProtectionManager.isMobIgnored(p);

        ProtectionManager.setMobIgnored(p, enabling);

        if (enabling) {
            // Immediately wipe any current aggro nearby so they stop chasing/attacking
            clearNearbyAggro(p, 32.0D);
            p.displayClientMessage(Component.literal("Mob Ignore enabled"), true);
        } else {
            // Optionally poke nearby mobs to re-evaluate you again
            reengageNearbyMobs(p, 32.0D);
            p.displayClientMessage(Component.literal("Mob Ignore disabled"), true);
        }
        return 1;
    }

    /** Null out targets for mobs currently targeting the player, stop pathing. */
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

    /** Encourage mobs to consider you again (used on disable). */
    private static void reengageNearbyMobs(ServerPlayer player, double range) {
        AABB box = player.getBoundingBox().inflate(range);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, box)) {
            if (!mob.isAlive() || !mob.isEffectiveAi()) continue;
            if (mob.getTarget() == player) {
                mob.setTarget(null); // clear stale target so AI can re-evaluate
            }
            // No forced targeting here—normal AI will pick up if appropriate.
            mob.getNavigation().stop();
        }
    }
}
