// ===============================================
// Centralizes state and applies abilities consistently.
// ===============================================

package com.phlompf.generalcore.commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "generalcore")
public final class ProtectionManager {
    private ProtectionManager() {}

    private static final Set<UUID> GOD = new HashSet<>();
    private static final Set<UUID> VANISH = new HashSet<>();
    private static final java.util.Set<java.util.UUID> IGNORE_MOBS = new java.util.HashSet<>();

    // --- GOD MODE ---
    public static boolean isGod(ServerPlayer p)            { return GOD.contains(p.getUUID()); }
    public static void setGod(ServerPlayer p, boolean on)  { if (on) GOD.add(p.getUUID()); else GOD.remove(p.getUUID()); }

    // --- VANISH ---
    public static boolean isVanished(ServerPlayer p)               { return VANISH.contains(p.getUUID()); }
    public static void setVanished(ServerPlayer p, boolean on)     { if (on) VANISH.add(p.getUUID()); else VANISH.remove(p.getUUID()); }

    // --- Mob Ignore ---
    public static boolean isMobIgnored(ServerPlayer p)            { return IGNORE_MOBS.contains(p.getUUID()); }
    public static void setMobIgnored(ServerPlayer p, boolean on)  { if (on) IGNORE_MOBS.add(p.getUUID()); else IGNORE_MOBS.remove(p.getUUID()); }

    /** Apply immediate ability flags (call on toggle, login, dim change). */
    public static void applyAbilities(ServerPlayer p) {
        boolean invul = isGod(p); // vanish does NOT control invulnerable
        if (p.getAbilities().invulnerable != invul) {
            p.getAbilities().invulnerable = invul;
            p.onUpdateAbilities();
        }
    }

    /** Hard guard: every server tick, enforce invulnerable exactly equals GOD state. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!(e.player instanceof ServerPlayer sp)) return;

        // Enforce invulnerability strictly from GOD
        boolean invul = isGod(sp);
        if (sp.getAbilities().invulnerable != invul) {
            sp.getAbilities().invulnerable = invul;
            sp.onUpdateAbilities();
        }

        // Enforce invisibility while vanished (do NOT force false when not vanished)
        if (isVanished(sp) && !sp.isInvisible()) {
            sp.setInvisible(true);
        }
    }

}