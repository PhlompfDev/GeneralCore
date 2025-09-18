package com.phlompf.generalcore.commands;

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "generalcore")
public class VanishEvents {

    // Whenever some player (tracker) starts tracking a vanished player, hide them again.
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof ServerPlayer target)) return;
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        if (ProtectionManager.isVanished(target)) {
            tracker.connection.send(new ClientboundRemoveEntitiesPacket(target.getId()));
        }
    }

    // When a player logs in, immediately hide all already-vanished players from them.
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        for (ServerPlayer maybeHidden : tracker.server.getPlayerList().getPlayers()) {
            if (ProtectionManager.isVanished(maybeHidden) && maybeHidden != tracker) {
                tracker.connection.send(new ClientboundRemoveEntitiesPacket(maybeHidden.getId()));
            }
        }
    }

    // When a player changes dimension, re-hide any vanished players now in view.
    @SubscribeEvent
    public static void onDimChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        for (ServerPlayer maybeHidden : tracker.server.getPlayerList().getPlayers()) {
            if (ProtectionManager.isVanished(maybeHidden) && maybeHidden != tracker) {
                tracker.connection.send(new ClientboundRemoveEntitiesPacket(maybeHidden.getId()));
            }
        }
    }

    // Kill sprint dust: if you're vanished, keep you from sprinting/swimming server-side
    // so the server never spawns sprint or swim particles.
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;
        if (!ProtectionManager.isVanished(sp)) return;

        // Prevent sprint dust & swim splashes
        if (sp.isSprinting()) sp.setSprinting(false);
        if (sp.isSwimming()) sp.setSwimming(false);
    }
}
