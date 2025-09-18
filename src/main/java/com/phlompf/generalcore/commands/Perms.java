package com.phlompf.generalcore.commands;

import com.phlompf.generalcore.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.UUID;

public final class Perms {
    private Perms() {}

    public static boolean isAllowed(CommandSourceStack src) {
        if (!(src.getEntity() instanceof ServerPlayer p)) return false;

        // First, UUID check (strongest)
        var allowedUUIDs = Config.ALLOWED_UUIDS.get();
        UUID myId = p.getUUID();
        for (String s : allowedUUIDs) {
            try {
                if (UUID.fromString(s.trim()).equals(myId)) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        // Fallback to case-insensitive name match
        var allowedNames = Config.ALLOWED_USERNAMES.get();
        String myName = p.getGameProfile().getName().toLowerCase(Locale.ROOT);
        for (String n : allowedNames) {
            if (myName.equals(n.toLowerCase(Locale.ROOT))) return true;
        }

        return false;
    }
}
