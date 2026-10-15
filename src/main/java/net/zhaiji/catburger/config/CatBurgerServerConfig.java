package net.zhaiji.catburger.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CatBurgerServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue COOLDOWN_DURATION = BUILDER
            .comment("Death protection cooldown, applies to every follower")
            .push("DeathProtectionCooldown")
            .comment("Ticks before death protection can trigger again, 0 disables the cooldown")
            .defineInRange(
                    "cooldownDuration",
                    36000,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.BooleanValue RESET_COOLDOWN_ON_WAKE_UP = BUILDER
            .comment("Waking up from sleep resets the cooldown")
            .define(
                    "resetCooldownOnWakeUp",
                    true
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_INTERVAL = BUILDER
            .pop()
            .comment("Settings for each follower")
            .push("Followers")
            .comment("Follower that restores food and protects from death while worn")
            .push("CatBurger")
            .comment("Effects that trigger repeatedly while the follower is worn")
            .push("TickEffects")
            .comment("Ticks between restorations, 0 restores every tick")
            .defineInRange(
                    "foodLevelRestorationInterval",
                    1200,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_AMOUNT = BUILDER
            .comment("Amount restored each time")
            .defineInRange(
                    "foodLevelRestorationAmount",
                    1,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_LIMIT = BUILDER
            .comment("Restoration stops once it reaches this value")
            .defineInRange(
                    "foodLevelRestorationLimit",
                    18,
                    1,
                    20
            );

    public static final ModConfigSpec.BooleanValue DEATH_PROTECTION_ENABLED = BUILDER
            .pop()
            .comment("Death protection while a follower is worn")
            .push("DeathProtectionEffect")
            .comment("Trigger when the wearer takes fatal damage")
            .define(
                    "deathProtectionEnabled",
                    true
            );

    public static final ModConfigSpec.BooleanValue USE_HEALTH_PERCENTAGE = BUILDER
            .comment("Health is restored as a percentage of max health, otherwise to a fixed value")
            .define(
                    "useHealthPercentage",
                    true
            );

    public static final ModConfigSpec.DoubleValue RESTORE_HEALTH_PERCENTAGE = BUILDER
            .comment("Health is restored to this percentage of max health")
            .defineInRange(
                    "restoreHealthPercentage",
                    100.0,
                    0.0,
                    100.0
            );

    public static final ModConfigSpec.IntValue RESTORE_HEALTH = BUILDER
            .comment("In fixed value mode, restored to this value")
            .defineInRange(
                    "restoreHealth",
                    20,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue RESTORE_FOOD_LEVEL = BUILDER
            .comment("Restored to this value")
            .defineInRange(
                    "restoreFoodLevel",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue RESTORE_SATURATION = BUILDER
            .comment("Restored to this value")
            .defineInRange(
                    "restoreSaturation",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.BooleanValue MOB_PICKUP_ENABLED = BUILDER
            .pop()
            .pop()
            .pop()
            .comment("Whether mobs can pick up and wear followers")
            .push("MobPickup")
            .comment("Allow mobs to pick up and wear followers")
            .define(
                    "mobPickupEnabled",
                    false
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().build();
}
