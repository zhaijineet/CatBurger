package net.zhaiji.catburger.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CatBurgerClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<FollowerRenderMode> FOLLOWER_RENDER_MODE = BUILDER
            .comment("Size and position of the follower relative to the wearer's eyes; offsets follow the orbit direction set by spring motion or rotation, not the follower's facing")
            .push("ScaleAndOffset")
            .comment("How multiple followers worn by the same wearer are arranged; independent lets each follower drift on its own, formation spreads them evenly around the wearer")
            .defineEnum(
                    "followerRenderMode",
                    FollowerRenderMode.INDEPENDENT
            );

    public static final ModConfigSpec.DoubleValue RENDER_SCALE = BUILDER
            .comment("Multiplied by the wearer's own scale")
            .defineInRange(
                    "renderScale",
                    0.5,
                    0.1,
                    2.0
            );

    public static final ModConfigSpec.DoubleValue FRONT_BACK_OFFSET = BUILDER
            .comment("Blocks to move forward along the orbit direction, negative moves backward")
            .defineInRange(
                    "frontBackOffset",
                    -0.4,
                    -5d,
                    5d
            );

    public static final ModConfigSpec.DoubleValue LEFT_RIGHT_OFFSET = BUILDER
            .comment("Blocks to move to the right of the orbit direction, negative moves left")
            .defineInRange(
                    "leftRightOffset",
                    0.8,
                    -5d,
                    5d
            );

    public static final ModConfigSpec.DoubleValue VERTICAL_OFFSET = BUILDER
            .comment("Blocks to move up from eye height, negative moves down")
            .defineInRange(
                    "verticalOffset",
                    0.5,
                    -5d,
                    5d
            );

    public static final ModConfigSpec.DoubleValue CROUCH_OFFSET = BUILDER
            .comment("Extra blocks to move up while sneaking, negative moves down")
            .defineInRange(
                    "crouchOffset",
                    -0.5,
                    -2d,
                    2d
            );

    public static final ModConfigSpec.DoubleValue FLOAT_DISTANCE = BUILDER
            .pop()
            .comment("Up and down floating motion")
            .push("FloatMotion")
            .comment("Height in blocks between the highest and lowest point of the float")
            .defineInRange(
                    "floatDistance",
                    0.1,
                    0,
                    5d
            );

    public static final ModConfigSpec.DoubleValue FLOAT_CYCLE_DURATION = BUILDER
            .comment("Ticks for one full float cycle")
            .defineInRange(
                    "floatCycleDuration",
                    60,
                    4,
                    400d
            );

    public static final ModConfigSpec.DoubleValue FOLLOW_SPEED = BUILDER
            .pop()
            .comment("Trailing behind the wearer while moving")
            .push("FollowDelay")
            .comment("How quickly the follower catches up, higher values give a shorter trail, 1.0 follows instantly")
            .defineInRange(
                    "followSpeed",
                    0.2,
                    0.01,
                    1.0
            );

    public static final ModConfigSpec.DoubleValue MAX_LAG_DISTANCE = BUILDER
            .comment("Maximum distance in blocks the follower trails behind, 0 disables trailing")
            .defineInRange(
                    "maxLagDistance",
                    3.0,
                    0.0,
                    5.0
            );

    public static final ModConfigSpec.BooleanValue SPRING_MOTION_ENABLED = BUILDER
            .comment("Spring motion for orbiting, overrides rotation when enabled")
            .push("SpringMotion")
            .comment("Orbit with spring motion that can overshoot and bounce back")
            .define(
                    "springMotionEnabled",
                    false
            );

    public static final ModConfigSpec.DoubleValue SPRING_STIFFNESS = BUILDER
            .comment("Spring stiffness, higher is snappier")
            .defineInRange(
                    "springStiffness",
                    0.9,
                    0.1,
                    2.0
            );

    public static final ModConfigSpec.DoubleValue SPRING_DAMPING = BUILDER
            .comment("Fraction of spring speed kept each tick, lower settles faster")
            .defineInRange(
                    "springDamping",
                    0.7,
                    0.1,
                    0.99
            );

    public static final ModConfigSpec.DoubleValue ROTATION_FOLLOW_SPEED = BUILDER
            .pop()
            .pop()
            .comment("How the follower orbits and faces when the wearer turns or looks around")
            .push("TurningAndFacing")
            .comment("How quickly the orbit catches up with the head, higher is faster, 1.0 follows instantly")
            .defineInRange(
                    "rotationFollowSpeed",
                    0.2,
                    0.01,
                    1.0
            );

    public static final ModConfigSpec.BooleanValue USE_SHORTEST_ARC = BUILDER
            .comment("Catch up along the shortest arc instead of following every full turn")
            .define(
                    "useShortestArc",
                    false
            );

    public static final ModConfigSpec.DoubleValue ORIENTATION_FOLLOW_SPEED = BUILDER
            .comment("How quickly the facing catches up with its target, higher is faster, 1.0 follows instantly")
            .defineInRange(
                    "orientationFollowSpeed",
                    0.5,
                    0.01,
                    1.0
            );

    public static final ModConfigSpec.BooleanValue GAZE_AT_VIEW_TARGET = BUILDER
            .comment("Aim the follower worn by a player at that player's own crosshair hit point, using the setting they synced to the server; followers on non-player wearers use this local setting")
            .define(
                    "gazeAtViewTarget",
                    true
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().build();
}
