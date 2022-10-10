package com.matyrobbrt.simpleminers.util;

import net.minecraftforge.common.ForgeConfigSpec;

public class SMConfig {
    public static final class Client {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.BooleanValue CATALYST_TOOLTIP;

        static {
            final var builder = new ForgeConfigSpec.Builder();

            CATALYST_TOOLTIP = builder.comment("If miner catalysts should have a tooltip added.")
                    .define("catalystTooltip", true);

            SPEC = builder.build();
        }
    }
}
