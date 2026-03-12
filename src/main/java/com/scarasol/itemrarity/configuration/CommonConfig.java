package com.scarasol.itemrarity.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Scarasol
 */
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AUTO_DOWNLOAD;

    static {

        AUTO_DOWNLOAD = BUILDER.comment(
                "Whether to automatically download and extract missing config files on startup (non-overwriting)."
        ).define("autoDownload", false);

        SPEC = BUILDER.build();
    }
}