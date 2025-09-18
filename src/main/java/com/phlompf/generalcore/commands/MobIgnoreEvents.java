package com.phlompf.generalcore.commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "generalcore")
public final class MobIgnoreEvents {

    /** Prevent mobs from selecting an ignored player as a target. */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent e) {
        if (!(e.getNewTarget() instanceof ServerPlayer sp)) return;
        if (ProtectionManager.isMobIgnored(sp)) {
            e.setCanceled(true);
        }
    }

    /** Block incoming attacks/damage from mobs to an ignored player (environmental still works). */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!ProtectionManager.isMobIgnored(sp)) return;

        var src = e.getSource().getEntity();
        if (src instanceof Mob) {
            e.setCanceled(true); // mobs cannot land attacks on ignored player
        }
    }

    /** Safety net at the damage stage (covers odd sources). */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        // 1) If victim is ignored player and source is a mob, cancel
        if (e.getEntity() instanceof ServerPlayer spVictim && ProtectionManager.isMobIgnored(spVictim)) {
            var src = e.getSource().getEntity();
            if (src instanceof Mob) {
                e.setCanceled(true);
                return;
            }
        }

        // 2) If attacker is an ignored player, let damage go through,
        //    but prevent retaliation/anger from the victim mob.
        var src = e.getSource().getEntity();
        if (src instanceof ServerPlayer spAttacker && ProtectionManager.isMobIgnored(spAttacker)) {
            if (e.getEntity() instanceof Mob mob) {
                var server = mob.level().getServer();
                if (server != null) {
                    server.execute(() -> clearRetaliation(mob));
                } else {
                    clearRetaliation(mob);
                }
            }
        }
    }

    private static void clearRetaliation(Mob mob) {
        if (!mob.isAlive() || !mob.isEffectiveAi()) return;
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        if (mob instanceof NeutralMob neutral) {
            neutral.setPersistentAngerTarget(null);
            neutral.setRemainingPersistentAngerTime(0);
        }
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }
}
