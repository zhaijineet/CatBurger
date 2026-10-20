package net.zhaiji.catburger.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CatBurgerServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PET_CONFIG_SYNC_ENABLED = BUILDER
            .comment("Whether the follower render settings of a player are copied to their pets; the server walks every level and entity on every config change, which can lag with many players and mobs, enable with care")
            .define(
                    "petConfigSyncEnabled",
                    false
            );

    public static final ModConfigSpec.BooleanValue MOB_ACTIVE_WEAR_ENABLED = BUILDER
            .comment("Whether mobs can actively wear followers")
            .define(
                    "mobActiveWearEnabled",
                    false
            );

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

    public static final ModConfigSpec.IntValue BLUB_TICK_EFFECT_INTERVAL = BUILDER
            .pop()
            .pop()
            .comment("Follower that applies effects while worn and protects the wearer from death")
            .push("Blub")
            .comment("Effects that trigger repeatedly while the follower is worn")
            .push("TickEffects")
            .comment("Ticks between refreshes, 0 refreshes every tick; keep it well below the effect duration or the effect expires and flickers")
            .defineInRange(
                    "tickEffectInterval",
                    100,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLUB_TICK_EFFECTS = BUILDER
            .comment("Effects granted on each refresh, one entry per line as effect|duration|amplifier, e.g. minecraft:night_vision|400|0")
            .defineListAllowEmpty(
                    "effects",
                    List.of("minecraft:night_vision|400|0"),
                    () -> "minecraft:night_vision|400|0",
                    ConfiguredEffect::isValidElement
            );

    public static final ModConfigSpec.BooleanValue BLUB_DEATH_PROTECTION_ENABLED = BUILDER
            .pop()
            .comment("Death protection while a follower is worn")
            .push("DeathProtectionEffect")
            .comment("Trigger when the wearer takes fatal damage")
            .define(
                    "deathProtectionEnabled",
                    true
            );

    public static final ModConfigSpec.BooleanValue BLUB_USE_HEALTH_PERCENTAGE = BUILDER
            .comment("Health is restored as a percentage of max health, otherwise to a fixed value")
            .define(
                    "useHealthPercentage",
                    true
            );

    public static final ModConfigSpec.DoubleValue BLUB_RESTORE_HEALTH_PERCENTAGE = BUILDER
            .comment("Health is restored to this percentage of max health")
            .defineInRange(
                    "restoreHealthPercentage",
                    100.0,
                    0.0,
                    100.0
            );

    public static final ModConfigSpec.IntValue BLUB_RESTORE_HEALTH = BUILDER
            .comment("In fixed value mode, restored to this value")
            .defineInRange(
                    "restoreHealth",
                    20,
                    0,
                    Integer.MAX_VALUE
            );

    public static final ModConfigSpec.DoubleValue BLUB_RADIUS = BUILDER
            .comment("Effects applied to nearby mobs when death protection triggers")
            .push("RadiusEffects")
            .comment("Radius in blocks around the wearer; which creatures are affected is set by the toggles below")
            .defineInRange(
                    "radius",
                    10.0,
                    0.0,
                    64.0
            );

    public static final ModConfigSpec.BooleanValue BLUB_RADIUS_AFFECT_SELF = BUILDER
            .comment("Whether the wearer itself is affected")
            .define(
                    "affectSelf",
                    false
            );

    public static final ModConfigSpec.BooleanValue BLUB_RADIUS_AFFECT_PLAYERS = BUILDER
            .comment("Whether players are affected")
            .define(
                    "affectPlayers",
                    false
            );

    public static final ModConfigSpec.BooleanValue BLUB_RADIUS_AFFECT_PETS = BUILDER
            .comment("Whether tamed pets are affected, meaning any creature with an owner")
            .define(
                    "affectPets",
                    false
            );

    public static final ModConfigSpec.BooleanValue BLUB_RADIUS_AFFECT_OTHER_MOBS = BUILDER
            .comment("Whether other mobs are affected, meaning creatures that are neither the wearer, nor players, nor tamed pets")
            .define(
                    "affectOtherMobs",
                    true
            );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLUB_RADIUS_EFFECTS = BUILDER
            .comment("Effects applied to every affected mob, one entry per line as effect|duration|amplifier, e.g. minecraft:weakness|100|0")
            .defineListAllowEmpty(
                    "effects",
                    List.of("minecraft:weakness|100|0", "minecraft:slowness|200|0"),
                    () -> "minecraft:weakness|100|0",
                    ConfiguredEffect::isValidElement
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().pop().pop().pop().build();
}
