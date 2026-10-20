package net.zhaiji.catburger.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CatBurgerClientConfig {
    // 下面每对边界与同名配置项的defineInRange成对维护，clampToBounds与配置界面都读这组常量，改一处即三处同步；
    // 之所以要把边界抄成常量，是因为ForgeConfigSpec.ConfigValue不暴露getSpec()与getRange()，取不到声明时的范围
    public static final double MIN_SPACING_LOWER = 0.5;
    public static final double MIN_SPACING_UPPER = 4.0;
    public static final double MAX_SPACING_LOWER = 0.5;
    public static final double MAX_SPACING_UPPER = 4.0;
    public static final double MAX_RADIUS_LOWER = 0.5;
    public static final double MAX_RADIUS_UPPER = 8.0;
    public static final double FORMATION_SPACING_LOWER = 0.5;
    public static final double FORMATION_SPACING_UPPER = 4.0;
    public static final double FORMATION_MAX_LAG_DISTANCE_LOWER = 0.0;
    public static final double FORMATION_MAX_LAG_DISTANCE_UPPER = 5.0;
    public static final double RENDER_SCALE_LOWER = 0.1;
    public static final double RENDER_SCALE_UPPER = 2.0;
    public static final double FRONT_BACK_OFFSET_LOWER = -5.0;
    public static final double FRONT_BACK_OFFSET_UPPER = 5.0;
    public static final double LEFT_RIGHT_OFFSET_LOWER = -5.0;
    public static final double LEFT_RIGHT_OFFSET_UPPER = 5.0;
    public static final double VERTICAL_OFFSET_LOWER = -5.0;
    public static final double VERTICAL_OFFSET_UPPER = 5.0;
    public static final double CROUCH_OFFSET_LOWER = -2.0;
    public static final double CROUCH_OFFSET_UPPER = 2.0;
    public static final double FLOAT_DISTANCE_LOWER = 0.0;
    public static final double FLOAT_DISTANCE_UPPER = 5.0;
    public static final double FLOAT_CYCLE_DURATION_LOWER = 4.0;
    public static final double FLOAT_CYCLE_DURATION_UPPER = 400.0;
    public static final double FOLLOW_SPEED_LOWER = 0.01;
    public static final double FOLLOW_SPEED_UPPER = 1.0;
    public static final double MAX_LAG_DISTANCE_LOWER = 0.0;
    public static final double MAX_LAG_DISTANCE_UPPER = 5.0;
    public static final double ROTATION_FOLLOW_SPEED_LOWER = 0.01;
    public static final double ROTATION_FOLLOW_SPEED_UPPER = 1.0;
    public static final double ORIENTATION_FOLLOW_SPEED_LOWER = 0.01;
    public static final double ORIENTATION_FOLLOW_SPEED_UPPER = 1.0;
    public static final double SPRING_STIFFNESS_LOWER = 0.1;
    public static final double SPRING_STIFFNESS_UPPER = 2.0;
    public static final double SPRING_DAMPING_LOWER = 0.1;
    public static final double SPRING_DAMPING_UPPER = 0.99;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<EntityDataFallback> ENTITY_DATA_FALLBACK = BUILDER
            .comment("Where entities without follower render data take their render settings from, LOCAL uses the current local values and CONFIG_DEFAULT uses the config defaults regardless of local changes")
            .defineEnum(
                    "entityDataFallback",
                    EntityDataFallback.LOCAL
            );

    public static final ForgeConfigSpec.EnumValue<FollowerRenderMode> FOLLOWER_RENDER_MODE = BUILDER
            .comment("How multiple followers worn by the same wearer are arranged")
            .push("MultipleFollowers")
            .comment("Independent scatters them loosely around the configured position, formation lines them up in a tail where each follows the one ahead")
            .defineEnum(
                    "followerRenderMode",
                    FollowerRenderMode.INDEPENDENT
            );

    public static final ForgeConfigSpec.BooleanValue DEDUPLICATE_RENDERING = BUILDER
            .comment("Render only one follower of each kind when the same follower is worn in several slots")
            .define(
                    "deduplicateRendering",
                    true
            );

    public static final ForgeConfigSpec.BooleanValue INDEPENDENT_FLOAT = BUILDER
            .comment("Give each follower its own float rhythm and period instead of floating in sync")
            .define(
                    "independentFloat",
                    true
            );

    public static final ForgeConfigSpec.DoubleValue MIN_SPACING = BUILDER
            .comment("Settings for independent mode, where each follower drifts on its own")
            .push("IndependentMode")
            .comment("Minimum blocks between followers")
            .defineInRange(
                    "minSpacing",
                    0.5,
                    MIN_SPACING_LOWER,
                    MIN_SPACING_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue MAX_SPACING = BUILDER
            .comment("Maximum blocks between followers")
            .defineInRange(
                    "maxSpacing",
                    0.8,
                    MAX_SPACING_LOWER,
                    MAX_SPACING_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue MAX_RADIUS = BUILDER
            .comment("Maximum horizontal distance in blocks a follower may drift from the wearer, independent mode only")
            .defineInRange(
                    "maxRadius",
                    2.0,
                    MAX_RADIUS_LOWER,
                    MAX_RADIUS_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FORMATION_SPACING = BUILDER
            .pop()
            .comment("Settings for formation mode, where followers line up in a tail")
            .push("FormationMode")
            .comment("Blocks between each follower and the one ahead of it")
            .defineInRange(
                    "formationSpacing",
                    0.8,
                    FORMATION_SPACING_LOWER,
                    FORMATION_SPACING_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FORMATION_MAX_LAG_DISTANCE = BUILDER
            .comment("Extra blocks a follower may be pulled beyond the formation spacing from the one ahead of it, 0 keeps it within the spacing; to stay out of the wearer's body it may be pushed up to about one wearer radius further")
            .defineInRange(
                    "formationMaxLagDistance",
                    0.5,
                    FORMATION_MAX_LAG_DISTANCE_LOWER,
                    FORMATION_MAX_LAG_DISTANCE_UPPER
            );

    public static final ForgeConfigSpec.EnumValue<FormationGazeTarget> FORMATION_GAZE_TARGET = BUILDER
            .comment("Where each follower in the tail looks: along the wearer's view, or at the follower ahead of it")
            .defineEnum(
                    "formationGazeTarget",
                    FormationGazeTarget.FRONT_FOLLOWER
            );

    public static final ForgeConfigSpec.BooleanValue SHOW_IN_FIRST_PERSON = BUILDER
            .pop()
            .pop()
            .comment("First person display")
            .push("FirstPersonDisplay")
            .comment("Show your own followers in first person")
            .define(
                    "showInFirstPerson",
                    true
            );

    public static final ForgeConfigSpec.DoubleValue RENDER_SCALE = BUILDER
            .pop()
            .comment("Size and position of the follower relative to the wearer's eyes; offsets follow the orbit direction set by spring motion or rotation, not the follower's facing")
            .push("ScaleAndOffset")
            .comment("Multiplied by the wearer's own scale")
            .defineInRange(
                    "renderScale",
                    0.5,
                    RENDER_SCALE_LOWER,
                    RENDER_SCALE_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FRONT_BACK_OFFSET = BUILDER
            .comment("Blocks to move forward along the orbit direction, negative moves backward")
            .defineInRange(
                    "frontBackOffset",
                    -0.4,
                    FRONT_BACK_OFFSET_LOWER,
                    FRONT_BACK_OFFSET_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue LEFT_RIGHT_OFFSET = BUILDER
            .comment("Blocks to move to the right of the orbit direction, negative moves left")
            .defineInRange(
                    "leftRightOffset",
                    0.8,
                    LEFT_RIGHT_OFFSET_LOWER,
                    LEFT_RIGHT_OFFSET_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue VERTICAL_OFFSET = BUILDER
            .comment("Blocks to move up from eye height, negative moves down")
            .defineInRange(
                    "verticalOffset",
                    0.5,
                    VERTICAL_OFFSET_LOWER,
                    VERTICAL_OFFSET_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue CROUCH_OFFSET = BUILDER
            .comment("Extra blocks to move up while sneaking, negative moves down")
            .defineInRange(
                    "crouchOffset",
                    -0.5,
                    CROUCH_OFFSET_LOWER,
                    CROUCH_OFFSET_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FLOAT_DISTANCE = BUILDER
            .pop()
            .comment("Up and down floating motion")
            .push("FloatMotion")
            .comment("Height in blocks between the highest and lowest point of the float")
            .defineInRange(
                    "floatDistance",
                    0.1,
                    FLOAT_DISTANCE_LOWER,
                    FLOAT_DISTANCE_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FLOAT_CYCLE_DURATION = BUILDER
            .comment("Ticks for one full float cycle")
            .defineInRange(
                    "floatCycleDuration",
                    60,
                    FLOAT_CYCLE_DURATION_LOWER,
                    FLOAT_CYCLE_DURATION_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue FOLLOW_SPEED = BUILDER
            .pop()
            .comment("Trailing behind the wearer while moving")
            .push("FollowDelay")
            .comment("How quickly the follower catches up, higher values give a shorter trail, 1.0 follows instantly")
            .defineInRange(
                    "followSpeed",
                    0.2,
                    FOLLOW_SPEED_LOWER,
                    FOLLOW_SPEED_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue MAX_LAG_DISTANCE = BUILDER
            .comment("Maximum distance in blocks the follower trails behind, 0 disables trailing")
            .defineInRange(
                    "maxLagDistance",
                    3.0,
                    MAX_LAG_DISTANCE_LOWER,
                    MAX_LAG_DISTANCE_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue ROTATION_FOLLOW_SPEED = BUILDER
            .pop()
            .comment("How the follower orbits and faces when the wearer turns or looks around")
            .push("TurningAndFacing")
            .comment("How quickly the orbit catches up with the head, higher is faster, 1.0 follows instantly")
            .defineInRange(
                    "rotationFollowSpeed",
                    0.2,
                    ROTATION_FOLLOW_SPEED_LOWER,
                    ROTATION_FOLLOW_SPEED_UPPER
            );

    public static final ForgeConfigSpec.BooleanValue USE_SHORTEST_ARC = BUILDER
            .comment("Catch up along the shortest arc instead of following every full turn")
            .define(
                    "useShortestArc",
                    false
            );

    public static final ForgeConfigSpec.DoubleValue ORIENTATION_FOLLOW_SPEED = BUILDER
            .comment("How quickly the facing catches up with its target, higher is faster, 1.0 follows instantly")
            .defineInRange(
                    "orientationFollowSpeed",
                    0.5,
                    ORIENTATION_FOLLOW_SPEED_LOWER,
                    ORIENTATION_FOLLOW_SPEED_UPPER
            );

    public static final ForgeConfigSpec.BooleanValue GAZE_AT_VIEW_TARGET = BUILDER
            .comment("Aim the follower worn by a player at that player's own crosshair hit point, using the setting they synced to the server; followers on non-player wearers use this local setting")
            .define(
                    "gazeAtViewTarget",
                    true
            );

    public static final ForgeConfigSpec.BooleanValue SPRING_MOTION_ENABLED = BUILDER
            .comment("Spring motion for orbiting, overrides rotation when enabled")
            .push("SpringMotion")
            .comment("Orbit with spring motion that can overshoot and bounce back")
            .define(
                    "springMotionEnabled",
                    false
            );

    public static final ForgeConfigSpec.DoubleValue SPRING_STIFFNESS = BUILDER
            .comment("Spring stiffness, higher is snappier")
            .defineInRange(
                    "springStiffness",
                    0.9,
                    SPRING_STIFFNESS_LOWER,
                    SPRING_STIFFNESS_UPPER
            );

    public static final ForgeConfigSpec.DoubleValue SPRING_DAMPING = BUILDER
            .comment("Fraction of spring speed kept each tick, lower settles faster")
            .defineInRange(
                    "springDamping",
                    0.7,
                    SPRING_DAMPING_LOWER,
                    SPRING_DAMPING_UPPER
            );

    public static final ForgeConfigSpec SPEC = BUILDER.pop().pop().build();
}
