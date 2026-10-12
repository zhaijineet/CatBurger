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

    public static final ModConfigSpec.IntValue DEATH_PROTECTION_COOLDOWN = BUILDER
            .comment("Ticks to wait before death protection can trigger again")
            .defineInRange(
                    "deathProtectionCooldown",
                    36000,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.BooleanValue DEATH_PROTECTION_USE_HEALTH_PERCENTAGE = BUILDER
            .comment("When enabled, health is restored as a percentage of max health, otherwise to a fixed value")
            .define(
                    "deathProtectionUseHealthPercentage",
                    true
            );

    public static final ModConfigSpec.DoubleValue DEATH_PROTECTION_RESTORE_HEALTH_PERCENTAGE = BUILDER
            .comment("In percentage mode, health is restored to this percentage of max health")
            .defineInRange(
                    "deathProtectionRestoreHealthPercentage",
                    100.0,
                    0.0,
                    100.0
            );

    public static final ModConfigSpec.IntValue DEATH_PROTECTION_RESTORE_HEALTH = BUILDER
            .comment("In fixed value mode, health is restored to this value")
            .defineInRange(
                    "deathProtectionRestoreHealth",
                    20,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue DEATH_PROTECTION_RESTORE_FOOD_LEVEL = BUILDER
            .comment("Food level is restored to this value when death protection triggers")
            .defineInRange(
                    "deathProtectionRestoreFoodLevel",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue DEATH_PROTECTION_RESTORE_SATURATION = BUILDER
            .comment("Saturation is restored to this value when death protection triggers")
            .defineInRange(
                    "deathProtectionRestoreSaturation",
                    20,
                    0,
                    20
            );

    public static final ModConfigSpec.BooleanValue WAKE_UP_RESETS_DEATH_PROTECTION_COOLDOWN = BUILDER
            .comment("Waking up from sleep resets the death protection cooldown")
            .define(
                    "wakeUpResetsDeathProtectionCooldown",
                    true
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_INTERVAL = BUILDER
            .pop()
            .comment("Food level restoration while a cat burger is worn")
            .push("FoodLevelRestoration")
            .comment("Ticks between food level restorations, 0 restores every tick")
            .defineInRange(
                    "foodLevelRestorationInterval",
                    1200,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_AMOUNT = BUILDER
            .comment("Food level restored each time")
            .defineInRange(
                    "foodLevelRestorationAmount",
                    1,
                    0,
                    20
            );

    public static final ModConfigSpec.IntValue FOOD_LEVEL_RESTORATION_LIMIT = BUILDER
            .comment("Food level stops being restored once it reaches this value")
            .defineInRange(
                    "foodLevelRestorationLimit",
                    18,
                    1,
                    20
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().build();
}
