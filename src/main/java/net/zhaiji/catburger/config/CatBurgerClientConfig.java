package net.zhaiji.catburger.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CatBurgerClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SCALE = BUILDER
            .comment("Size and position of the cat burger relative to the wearer's eyes")
            .push("Position")
            .comment("Render scale of the cat burger")
            .defineInRange(
                    "scale",
                    0.7,
                    0.1,
                    10d
            );

    public static final ModConfigSpec.DoubleValue FRONT_BACK_OFFSET = BUILDER
            .comment("Blocks to move forward along the facing direction, negative moves backward")
            .defineInRange(
                    "frontBackOffset",
                    -0.4,
                    -100d,
                    100d
            );

    public static final ModConfigSpec.DoubleValue LEFT_RIGHT_OFFSET = BUILDER
            .comment("Blocks to move to the right, negative moves left")
            .defineInRange(
                    "leftRightOffset",
                    0.8,
                    -100d,
                    100d
            );

    public static final ModConfigSpec.DoubleValue VERTICAL_OFFSET = BUILDER
            .comment("Blocks to move up from eye height, negative moves down")
            .defineInRange(
                    "verticalOffset",
                    0,
                    -100d,
                    100d
            );

    public static final ModConfigSpec.DoubleValue CROUCH_OFFSET = BUILDER
            .comment("Extra blocks to move up while sneaking, negative moves down")
            .defineInRange(
                    "crouchOffset",
                    -0.5,
                    -10d,
                    10d
            );

    public static final ModConfigSpec.DoubleValue FLOAT_DISTANCE = BUILDER
            .pop()
            .comment("Up and down floating motion")
            .push("Float")
            .comment("Height in blocks between the highest and lowest point of the float")
            .defineInRange(
                    "floatDistance",
                    0.1,
                    0,
                    100d
            );

    public static final ModConfigSpec.DoubleValue FLOAT_CYCLE_DURATION = BUILDER
            .comment("Ticks for one full float cycle")
            .defineInRange(
                    "floatCycleDuration",
                    60,
                    4,
                    400d
            );

    public static final ModConfigSpec.BooleanValue DRAG_ENABLED = BUILDER
            .pop()
            .comment("Trailing behind the wearer while moving")
            .push("PositionDrag")
            .comment("Let the cat burger trail behind while the wearer moves")
            .define(
                    "dragEnabled",
                    true
            );

    public static final ModConfigSpec.DoubleValue DRAG_STRENGTH = BUILDER
            .comment("How quickly the cat burger catches up, higher values give a shorter trail")
            .defineInRange(
                    "dragStrength",
                    0.2,
                    0.01,
                    1.0
            );

    public static final ModConfigSpec.DoubleValue MAX_LAG_DISTANCE = BUILDER
            .comment("Maximum distance in blocks the cat burger trails behind, 0 disables trailing")
            .defineInRange(
                    "maxLagDistance",
                    3.0,
                    0.0,
                    10.0
            );

    public static final ModConfigSpec.BooleanValue ROTATION_DRAG_ENABLED = BUILDER
            .pop()
            .comment("Delayed orbiting when the wearer turns")
            .push("RotationDrag")
            .comment("Let the cat burger orbit after the wearer's head turns with a delay")
            .define(
                    "rotationDragEnabled",
                    true
            );

    public static final ModConfigSpec.DoubleValue ROTATION_DRAG_SMOOTHNESS = BUILDER
            .comment("How quickly the orbit catches up with the head, higher is faster")
            .defineInRange(
                    "rotationDragSmoothness",
                    0.2,
                    0.01,
                    1.0
            );

    public static final ModConfigSpec.BooleanValue ROTATION_DRAG_USE_WRAP_DEGREES = BUILDER
            .comment("Catch up along the shortest arc instead of following every full turn")
            .define(
                    "rotationDragUseWrapDegrees",
                    false
            );

    public static final ModConfigSpec.BooleanValue ROTATION_DRAG_AFFECT_ORIENTATION = BUILDER
            .comment("Make the cat burger face the delayed direction instead of the view direction")
            .define(
                    "rotationDragAffectOrientation",
                    false
            );

    public static final ModConfigSpec.BooleanValue SPRING_ENABLED = BUILDER
            .pop()
            .comment("Spring motion for orbiting, overrides rotation drag when enabled")
            .push("Spring")
            .comment("Orbit with spring motion that can overshoot and bounce back")
            .define(
                    "springEnabled",
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

    public static final ModConfigSpec SPEC = BUILDER.pop().build();
}
