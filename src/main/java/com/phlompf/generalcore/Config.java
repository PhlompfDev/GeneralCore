package com.phlompf.generalcore;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class Config {
    private Config() {}

    public static final ForgeConfigSpec SPEC;

    // Global allow-lists (usernames and/or UUIDs). Case-insensitive for names.
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_USERNAMES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_UUIDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("permissions");

        ALLOWED_USERNAMES = b
                .comment("Usernames allowed to use hidden/admin commands (case-insensitive).")
                .defineListAllowEmpty(
                        List.of("allowedUsernames"),
                        () -> List.of("Plomph", "Dev"),
                        o -> o instanceof String && !((String) o).isBlank()
                );

        ALLOWED_UUIDS = b
                .comment("Player UUIDs allowed to use commands (recommended if names change).")
                .defineListAllowEmpty(
                        List.of("allowedUUIDs"),
                        List::of,
                        o -> o instanceof String && !((String) o).isBlank()
                );

        b.pop();

        SPEC = b.build();
    }
}
