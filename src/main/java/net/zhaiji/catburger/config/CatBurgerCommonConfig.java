package net.zhaiji.catburger.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CatBurgerCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEATH_PROTECTION_ENABLED = BUILDER
            .comment("Death protection while a cat burger is worn")
            .push("DeathProtection")
            .comment("Trigger death protection when the wearer takes fatal damage")
            .define(
                    "deathProtectionEnabled",
                    true
            );

    public static final ModConfigSpec.IntValue COOLDOWN = BUILDER
            .comment("Ticks to wait before death protection can trigger again, 0 disables the cooldown")
            .defineInRange(
                    "cooldown",
                    36000,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.BooleanValue USE_HEALTH_PERCENTAGE = BUILDER
            .comment("When enabled, health is restored as a percentage of max health, otherwise to a fixed value")
            .define(
                    "useHealthPercentage",
                    true
            );

    public static final ModConfigSpec.DoubleValue RESTORE_HEALTH_PERCENTAGE = BUILDER
            .comment("In percentage mode, health is restored to this percentage of max health")
            .defineInRange(
                    "restoreHealthPercentage",
                    100.0,
                    0.0,
                    100.0
            );

    public static final ModConfigSpec.IntValue RESTORE_HEALTH = BUILDER
            .comment("In fixed value mode, health is restored to this value")
            .defineInRange(
                    "restoreHealth",
                    20,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue RESTORE_FOOD_LEVEL = BUILDER
            .comment("Food level is restored to this value when death protection triggers")
            .defineInRange(
                    "restoreFoodLevel",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue RESTORE_SATURATION = BUILDER
            .comment("Saturation is restored to this value when death protection triggers")
            .defineInRange(
                    "restoreSaturation",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.BooleanValue RESET_COOLDOWN_ON_WAKE_UP = BUILDER
            .comment("Waking up from sleep resets the wearer's death protection cooldown")
            .define(
                    "resetCooldownOnWakeUp",
                    true
            );

    public static final ModConfigSpec.IntValue INTERVAL = BUILDER
            .pop()
            .comment("Food level restoration while a cat burger is worn")
            .push("FoodLevelRestoration")
            .comment("Ticks between food level restorations, 0 restores every tick")
            .defineInRange(
                    "interval",
                    1200,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue AMOUNT = BUILDER
            .comment("Food level restored each time")
            .defineInRange(
                    "amount",
                    1,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue LIMIT = BUILDER
            .comment("Food level stops being restored once it reaches this value")
            .defineInRange(
                    "limit",
                    18,
                    1,
                    20
            );

    public static final ModConfigSpec.BooleanValue MOB_PICKUP_ENABLED = BUILDER
            .pop()
            .comment("Whether mobs can pick up and wear cat burgers")
            .push("MobPickup")
            .comment("Allow mobs to pick up and wear cat burgers")
            .define(
                    "mobPickupEnabled",
                    false
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().build();
}
