package net.zhaiji.catburger.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;

/**
 * 一名玩家用于渲染其跟随者的全部参数：客户端按本机配置填好后上传，服务端收束并广播给追踪者。
 */
public class FollowerRenderData implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, FollowerRenderData> STREAM_CODEC = StreamCodec.of(
            FollowerRenderData::encode,
            FollowerRenderData::decode
    );

    private FollowerRenderMode renderMode = CatBurgerClientConfig.FOLLOWER_RENDER_MODE.getDefault();
    private boolean deduplicateRendering = CatBurgerClientConfig.DEDUPLICATE_RENDERING.getDefault();
    private boolean independentFloat = CatBurgerClientConfig.INDEPENDENT_FLOAT.getDefault();
    private double minSpacing = CatBurgerClientConfig.MIN_SPACING.getDefault();
    private double maxSpacing = CatBurgerClientConfig.MAX_SPACING.getDefault();
    private double maxRadius = CatBurgerClientConfig.MAX_RADIUS.getDefault();
    private double formationSpacing = CatBurgerClientConfig.FORMATION_SPACING.getDefault();
    private double formationMaxLagDistance = CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE.getDefault();
    private FormationGazeTarget formationGazeTarget = CatBurgerClientConfig.FORMATION_GAZE_TARGET.getDefault();
    private double scale = CatBurgerClientConfig.RENDER_SCALE.getDefault();
    private double frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.getDefault();
    private double leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.getDefault();
    private double verticalOffset = CatBurgerClientConfig.VERTICAL_OFFSET.getDefault();
    private double crouchOffset = CatBurgerClientConfig.CROUCH_OFFSET.getDefault();

    private double floatDistance = CatBurgerClientConfig.FLOAT_DISTANCE.getDefault();
    private double floatCycleDuration = CatBurgerClientConfig.FLOAT_CYCLE_DURATION.getDefault();

    private double followSpeed = CatBurgerClientConfig.FOLLOW_SPEED.getDefault();
    private double maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.getDefault();

    private boolean springMotionEnabled = CatBurgerClientConfig.SPRING_MOTION_ENABLED.getDefault();
    private double springStiffness = CatBurgerClientConfig.SPRING_STIFFNESS.getDefault();
    private double springDamping = CatBurgerClientConfig.SPRING_DAMPING.getDefault();

    private double rotationFollowSpeed = CatBurgerClientConfig.ROTATION_FOLLOW_SPEED.getDefault();
    private boolean useShortestArc = CatBurgerClientConfig.USE_SHORTEST_ARC.getDefault();
    private double orientationFollowSpeed = CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED.getDefault();

    private boolean gazeAtViewTarget = CatBurgerClientConfig.GAZE_AT_VIEW_TARGET.getDefault();

    /**
     * 按本机客户端配置填充全部字段，只在客户端发送前调用。
     */
    public void loadFromClientConfig() {
        renderMode = CatBurgerClientConfig.FOLLOWER_RENDER_MODE.get();
        deduplicateRendering = CatBurgerClientConfig.DEDUPLICATE_RENDERING.get();
        independentFloat = CatBurgerClientConfig.INDEPENDENT_FLOAT.get();
        minSpacing = CatBurgerClientConfig.MIN_SPACING.get();
        maxSpacing = CatBurgerClientConfig.MAX_SPACING.get();
        maxRadius = CatBurgerClientConfig.MAX_RADIUS.get();
        formationSpacing = CatBurgerClientConfig.FORMATION_SPACING.get();
        formationMaxLagDistance = CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE.get();
        formationGazeTarget = CatBurgerClientConfig.FORMATION_GAZE_TARGET.get();
        scale = CatBurgerClientConfig.RENDER_SCALE.get();
        frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.get();
        leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.get();
        verticalOffset = CatBurgerClientConfig.VERTICAL_OFFSET.get();
        crouchOffset = CatBurgerClientConfig.CROUCH_OFFSET.get();
        floatDistance = CatBurgerClientConfig.FLOAT_DISTANCE.get();
        floatCycleDuration = CatBurgerClientConfig.FLOAT_CYCLE_DURATION.get();
        followSpeed = CatBurgerClientConfig.FOLLOW_SPEED.get();
        maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.get();
        springMotionEnabled = CatBurgerClientConfig.SPRING_MOTION_ENABLED.get();
        springStiffness = CatBurgerClientConfig.SPRING_STIFFNESS.get();
        springDamping = CatBurgerClientConfig.SPRING_DAMPING.get();
        rotationFollowSpeed = CatBurgerClientConfig.ROTATION_FOLLOW_SPEED.get();
        useShortestArc = CatBurgerClientConfig.USE_SHORTEST_ARC.get();
        orientationFollowSpeed = CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED.get();
        gazeAtViewTarget = CatBurgerClientConfig.GAZE_AT_VIEW_TARGET.get();
    }

    /**
     * 复制一份独立实例，供有主生物持有主人的参数，避免有主生物与主人共享同一个可变实例，复用附件序列化以免再维护第二份字段清单。
     * TagValueInput的HolderLookup.Provider传null，因为本类的deserialize只读基本类型，从不按Codec取注册表引用，ValueInput不会用到它；
     * 将来若新增Holder这类注册表型字段，必须在这里补上真实provider，否则该字段会被静默丢弃，因为DISCARDING不会报读取失败。
     */
    public FollowerRenderData copy() {
        FollowerRenderData copiedData = new FollowerRenderData();
        copiedData.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, null, serializeToTag()));
        return copiedData;
    }

    /**
     * 按序列化结果比较两份参数是否完全一致，用于跳过没有变化的副本写入与广播。
     */
    public boolean matches(FollowerRenderData other) {
        return serializeToTag().equals(other.serializeToTag());
    }

    private CompoundTag serializeToTag() {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        serialize(output);
        return output.buildResult();
    }

    /**
     * 把各数值字段收束到配置声明的边界内，用于校验跨端传入的值。
     */
    public void clampToBounds() {
        minSpacing = clampToConfig(CatBurgerClientConfig.MIN_SPACING, minSpacing);
        maxSpacing = clampToConfig(CatBurgerClientConfig.MAX_SPACING, maxSpacing);
        maxRadius = clampToConfig(CatBurgerClientConfig.MAX_RADIUS, maxRadius);
        formationSpacing = clampToConfig(CatBurgerClientConfig.FORMATION_SPACING, formationSpacing);
        formationMaxLagDistance = clampToConfig(CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE, formationMaxLagDistance);
        scale = clampToConfig(CatBurgerClientConfig.RENDER_SCALE, scale);
        frontBackOffset = clampToConfig(CatBurgerClientConfig.FRONT_BACK_OFFSET, frontBackOffset);
        leftRightOffset = clampToConfig(CatBurgerClientConfig.LEFT_RIGHT_OFFSET, leftRightOffset);
        verticalOffset = clampToConfig(CatBurgerClientConfig.VERTICAL_OFFSET, verticalOffset);
        crouchOffset = clampToConfig(CatBurgerClientConfig.CROUCH_OFFSET, crouchOffset);
        floatDistance = clampToConfig(CatBurgerClientConfig.FLOAT_DISTANCE, floatDistance);
        floatCycleDuration = clampToConfig(CatBurgerClientConfig.FLOAT_CYCLE_DURATION, floatCycleDuration);
        followSpeed = clampToConfig(CatBurgerClientConfig.FOLLOW_SPEED, followSpeed);
        maxLagDistance = clampToConfig(CatBurgerClientConfig.MAX_LAG_DISTANCE, maxLagDistance);
        springStiffness = clampToConfig(CatBurgerClientConfig.SPRING_STIFFNESS, springStiffness);
        springDamping = clampToConfig(CatBurgerClientConfig.SPRING_DAMPING, springDamping);
        rotationFollowSpeed = clampToConfig(CatBurgerClientConfig.ROTATION_FOLLOW_SPEED, rotationFollowSpeed);
        orientationFollowSpeed = clampToConfig(CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED, orientationFollowSpeed);
    }

    /**
     * 收束到配置自己声明的边界内，非有限值回落到配置默认值，避免 NaN 与 Infinity 进入渲染矩阵。
     */
    private static double clampToConfig(ModConfigSpec.DoubleValue value, double rawValue) {
        if (!Double.isFinite(rawValue)) return value.getDefault();
        ModConfigSpec.Range<Double> range = value.getSpec().getRange();
        return Mth.clamp(rawValue, range.getMin(), range.getMax());
    }

    private static void encode(RegistryFriendlyByteBuf buf, FollowerRenderData data) {
        FollowerRenderMode.STREAM_CODEC.encode(buf, data.renderMode);
        buf.writeBoolean(data.deduplicateRendering);
        buf.writeBoolean(data.independentFloat);
        buf.writeDouble(data.minSpacing);
        buf.writeDouble(data.maxSpacing);
        buf.writeDouble(data.maxRadius);
        buf.writeDouble(data.formationSpacing);
        buf.writeDouble(data.formationMaxLagDistance);
        FormationGazeTarget.STREAM_CODEC.encode(buf, data.formationGazeTarget);
        buf.writeDouble(data.scale);
        buf.writeDouble(data.frontBackOffset);
        buf.writeDouble(data.leftRightOffset);
        buf.writeDouble(data.verticalOffset);
        buf.writeDouble(data.crouchOffset);
        buf.writeDouble(data.floatDistance);
        buf.writeDouble(data.floatCycleDuration);
        buf.writeDouble(data.followSpeed);
        buf.writeDouble(data.maxLagDistance);
        buf.writeBoolean(data.springMotionEnabled);
        buf.writeDouble(data.springStiffness);
        buf.writeDouble(data.springDamping);
        buf.writeDouble(data.rotationFollowSpeed);
        buf.writeBoolean(data.useShortestArc);
        buf.writeDouble(data.orientationFollowSpeed);
        buf.writeBoolean(data.gazeAtViewTarget);
    }

    private static FollowerRenderData decode(RegistryFriendlyByteBuf buf) {
        FollowerRenderData data = new FollowerRenderData();
        data.renderMode = FollowerRenderMode.STREAM_CODEC.decode(buf);
        data.deduplicateRendering = buf.readBoolean();
        data.independentFloat = buf.readBoolean();
        data.minSpacing = buf.readDouble();
        data.maxSpacing = buf.readDouble();
        data.maxRadius = buf.readDouble();
        data.formationSpacing = buf.readDouble();
        data.formationMaxLagDistance = buf.readDouble();
        data.formationGazeTarget = FormationGazeTarget.STREAM_CODEC.decode(buf);
        data.scale = buf.readDouble();
        data.frontBackOffset = buf.readDouble();
        data.leftRightOffset = buf.readDouble();
        data.verticalOffset = buf.readDouble();
        data.crouchOffset = buf.readDouble();
        data.floatDistance = buf.readDouble();
        data.floatCycleDuration = buf.readDouble();
        data.followSpeed = buf.readDouble();
        data.maxLagDistance = buf.readDouble();
        data.springMotionEnabled = buf.readBoolean();
        data.springStiffness = buf.readDouble();
        data.springDamping = buf.readDouble();
        data.rotationFollowSpeed = buf.readDouble();
        data.useShortestArc = buf.readBoolean();
        data.orientationFollowSpeed = buf.readDouble();
        data.gazeAtViewTarget = buf.readBoolean();
        return data;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("renderMode", renderMode.getSerializedName());
        output.putBoolean("deduplicateRendering", deduplicateRendering);
        output.putBoolean("independentFloat", independentFloat);
        output.putDouble("minSpacing", minSpacing);
        output.putDouble("maxSpacing", maxSpacing);
        output.putDouble("maxRadius", maxRadius);
        output.putDouble("formationSpacing", formationSpacing);
        output.putDouble("formationMaxLagDistance", formationMaxLagDistance);
        output.putString("formationGazeTarget", formationGazeTarget.getSerializedName());
        output.putDouble("scale", scale);
        output.putDouble("frontBackOffset", frontBackOffset);
        output.putDouble("leftRightOffset", leftRightOffset);
        output.putDouble("verticalOffset", verticalOffset);
        output.putDouble("crouchOffset", crouchOffset);
        output.putDouble("floatDistance", floatDistance);
        output.putDouble("floatCycleDuration", floatCycleDuration);
        output.putDouble("followSpeed", followSpeed);
        output.putDouble("maxLagDistance", maxLagDistance);
        output.putBoolean("springMotionEnabled", springMotionEnabled);
        output.putDouble("springStiffness", springStiffness);
        output.putDouble("springDamping", springDamping);
        output.putDouble("rotationFollowSpeed", rotationFollowSpeed);
        output.putBoolean("useShortestArc", useShortestArc);
        output.putDouble("orientationFollowSpeed", orientationFollowSpeed);
        output.putBoolean("gazeAtViewTarget", gazeAtViewTarget);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getString("renderMode").ifPresent(name -> renderMode = FollowerRenderMode.byName(name));
        deduplicateRendering = input.getBooleanOr("deduplicateRendering", deduplicateRendering);
        independentFloat = input.getBooleanOr("independentFloat", independentFloat);
        minSpacing = input.getDoubleOr("minSpacing", minSpacing);
        maxSpacing = input.getDoubleOr("maxSpacing", maxSpacing);
        maxRadius = input.getDoubleOr("maxRadius", maxRadius);
        formationSpacing = input.getDoubleOr("formationSpacing", formationSpacing);
        formationMaxLagDistance = input.getDoubleOr("formationMaxLagDistance", formationMaxLagDistance);
        input.getString("formationGazeTarget").ifPresent(name -> formationGazeTarget = FormationGazeTarget.byName(name));
        scale = input.getDoubleOr("scale", scale);
        frontBackOffset = input.getDoubleOr("frontBackOffset", frontBackOffset);
        leftRightOffset = input.getDoubleOr("leftRightOffset", leftRightOffset);
        verticalOffset = input.getDoubleOr("verticalOffset", verticalOffset);
        crouchOffset = input.getDoubleOr("crouchOffset", crouchOffset);
        floatDistance = input.getDoubleOr("floatDistance", floatDistance);
        floatCycleDuration = input.getDoubleOr("floatCycleDuration", floatCycleDuration);
        followSpeed = input.getDoubleOr("followSpeed", followSpeed);
        maxLagDistance = input.getDoubleOr("maxLagDistance", maxLagDistance);
        springMotionEnabled = input.getBooleanOr("springMotionEnabled", springMotionEnabled);
        springStiffness = input.getDoubleOr("springStiffness", springStiffness);
        springDamping = input.getDoubleOr("springDamping", springDamping);
        rotationFollowSpeed = input.getDoubleOr("rotationFollowSpeed", rotationFollowSpeed);
        useShortestArc = input.getBooleanOr("useShortestArc", useShortestArc);
        orientationFollowSpeed = input.getDoubleOr("orientationFollowSpeed", orientationFollowSpeed);
        gazeAtViewTarget = input.getBooleanOr("gazeAtViewTarget", gazeAtViewTarget);
    }

    public FollowerRenderMode getRenderMode() {
        return renderMode;
    }

    public double getScale() {
        return scale;
    }

    public double getFrontBackOffset() {
        return frontBackOffset;
    }

    public double getLeftRightOffset() {
        return leftRightOffset;
    }

    public double getVerticalOffset() {
        return verticalOffset;
    }

    public double getCrouchOffset() {
        return crouchOffset;
    }

    public double getFloatDistance() {
        return floatDistance;
    }

    public double getFloatCycleDuration() {
        return floatCycleDuration;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public double getMaxLagDistance() {
        return maxLagDistance;
    }

    public boolean isSpringMotionEnabled() {
        return springMotionEnabled;
    }

    public double getSpringStiffness() {
        return springStiffness;
    }

    public double getSpringDamping() {
        return springDamping;
    }

    public double getRotationFollowSpeed() {
        return rotationFollowSpeed;
    }

    public boolean isUseShortestArc() {
        return useShortestArc;
    }

    public double getOrientationFollowSpeed() {
        return orientationFollowSpeed;
    }

    public boolean isGazeAtViewTarget() {
        return gazeAtViewTarget;
    }

    public boolean isDeduplicateRendering() {
        return deduplicateRendering;
    }

    public boolean isIndependentFloat() {
        return independentFloat;
    }

    public double getMinSpacing() {
        return minSpacing;
    }

    public double getMaxSpacing() {
        return maxSpacing;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    public double getFormationSpacing() {
        return formationSpacing;
    }

    public double getFormationMaxLagDistance() {
        return formationMaxLagDistance;
    }

    public FormationGazeTarget getFormationGazeTarget() {
        return formationGazeTarget;
    }
}
