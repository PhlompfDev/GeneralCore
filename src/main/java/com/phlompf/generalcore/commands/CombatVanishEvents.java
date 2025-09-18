package com.phlompf.generalcore.commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "generalcore")
public final class CombatVanishEvents {

    // Block attacks/damage against a vanished player (others can't hurt you while vanished)
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && ProtectionManager.isVanished(sp)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        // 1) If the victim is *you* and you're vanished, cancel the damage.
        if (e.getEntity() instanceof ServerPlayer spVictim && ProtectionManager.isVanished(spVictim)) {
            e.setCanceled(true);
            return;
        }

        // 2) If the attacker is a vanished player, let the damage go through,
        //    but prevent the victim mob from aggroing/retaliating.
        var src = e.getSource().getEntity();
        if (src instanceof ServerPlayer spAttacker && ProtectionManager.isVanished(spAttacker)) {
            if (e.getEntity() instanceof Mob mob) {
                // Run next tick so we override HurtByTargetGoal that sets retaliation
                var server = mob.level().getServer();
                if (server != null) {
                    server.execute(() -> clearRetaliation(mob));
                } else {
                    // Fallback (singleplayer edge): do it immediately
                    clearRetaliation(mob);
                }
            }
        }
    }

    // Clears any current/queued aggro so the mob doesn't target a vanished attacker
    private static void clearRetaliation(Mob mob) {
        if (!mob.isAlive() || !mob.isEffectiveAi()) return;

        // Drop any target/retaliation state
        mob.setTarget(null);
        mob.setLastHurtByMob(null);

        // Neutral mobs with persistent anger (Endermen, Bees, Piglins, Wolves, etc.)
        if (mob instanceof NeutralMob neutral) {
            neutral.setPersistentAngerTarget(null);
            neutral.setRemainingPersistentAngerTime(0);
        }

        // De-aggress to be safe (some AIs check this)
        mob.setAggressive(false);

        // Stop navigation so they don't keep pathing to your last position
        mob.getNavigation().stop();
    }
}
