package com.phlompf.generalcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderPlayerEvent;

@Mod.EventBusSubscriber(modid = "generalcore", value = Dist.CLIENT)
public class VanishClientEvents {

    // Hide your own model (and thus armor) in third-person while invisible.
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (me == null) return;

        if (event.getEntity().getUUID().equals(me.getUUID()) && event.getEntity().isInvisible()) {
            // Cancel rendering of *your* player entirely => no armor, no body in 3rd person
            event.setCanceled(true);
        }
    }

    // Kill local sprint/swim every client tick while invisible so client doesn't spawn particles.
    @SubscribeEvent
    public static void onPlayerTickClient(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !event.player.getUUID().equals(mc.player.getUUID())) return;

        if (event.player.isInvisible()) {
            if (event.player.isSprinting()) event.player.setSprinting(false);
            if (event.player.isSwimming()) event.player.setSwimming(false);
        }
    }
}
