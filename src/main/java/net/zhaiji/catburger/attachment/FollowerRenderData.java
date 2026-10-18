package net.zhaiji.catburger.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;

/**
 * 一名玩家用于渲染其跟随者的全部参数：客户端按本机配置填好后上传，服务端收束并广播给追踪者。
 */
public class FollowerRenderData implements INBTSerializable<CompoundTag> {
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("renderMode", renderMode.getSerializedName());
        tag.putBoolean("deduplicateRendering", deduplicateRendering);
        tag.putBoolean("independentFloat", independentFloat);
        tag.putDouble("minSpacing", minSpacing);
        tag.putDouble("maxSpacing", maxSpacing);
        tag.putDouble("maxRadius", maxRadius);
        tag.putDouble("formationSpacing", formationSpacing);
        tag.putDouble("formationMaxLagDistance", formationMaxLagDistance);
        tag.putString("formationGazeTarget", formationGazeTarget.getSerializedName());
        tag.putDouble("scale", scale);
        tag.putDouble("frontBackOffset", frontBackOffset);
        tag.putDouble("leftRightOffset", leftRightOffset);
        tag.putDouble("verticalOffset", verticalOffset);
        tag.putDouble("crouchOffset", crouchOffset);
        tag.putDouble("floatDistance", floatDistance);
        tag.putDouble("floatCycleDuration", floatCycleDuration);
        tag.putDouble("followSpeed", followSpeed);
        tag.putDouble("maxLagDistance", maxLagDistance);
        tag.putBoolean("springMotionEnabled", springMotionEnabled);
        tag.putDouble("springStiffness", springStiffness);
        tag.putDouble("springDamping", springDamping);
        tag.putDouble("rotationFollowSpeed", rotationFollowSpeed);
        tag.putBoolean("useShortestArc", useShortestArc);
        tag.putDouble("orientationFollowSpeed", orientationFollowSpeed);
        tag.putBoolean("gazeAtViewTarget", gazeAtViewTarget);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("renderMode")) renderMode = FollowerRenderMode.byName(tag.getString("renderMode"));
        if (tag.contains("deduplicateRendering")) deduplicateRendering = tag.getBoolean("deduplicateRendering");
        if (tag.contains("independentFloat")) independentFloat = tag.getBoolean("independentFloat");
        if (tag.contains("minSpacing")) minSpacing = tag.getDouble("minSpacing");
        if (tag.contains("maxSpacing")) maxSpacing = tag.getDouble("maxSpacing");
        if (tag.contains("maxRadius")) maxRadius = tag.getDouble("maxRadius");
        if (tag.contains("formationSpacing")) formationSpacing = tag.getDouble("formationSpacing");
        if (tag.contains("formationMaxLagDistance")) formationMaxLagDistance = tag.getDouble("formationMaxLagDistance");
        if (tag.contains("formationGazeTarget")) formationGazeTarget = FormationGazeTarget.byName(tag.getString("formationGazeTarget"));
        if (tag.contains("scale")) scale = tag.getDouble("scale");
        if (tag.contains("frontBackOffset")) frontBackOffset = tag.getDouble("frontBackOffset");
        if (tag.contains("leftRightOffset")) leftRightOffset = tag.getDouble("leftRightOffset");
        if (tag.contains("verticalOffset")) verticalOffset = tag.getDouble("verticalOffset");
        if (tag.contains("crouchOffset")) crouchOffset = tag.getDouble("crouchOffset");
        if (tag.contains("floatDistance")) floatDistance = tag.getDouble("floatDistance");
        if (tag.contains("floatCycleDuration")) floatCycleDuration = tag.getDouble("floatCycleDuration");
        if (tag.contains("followSpeed")) followSpeed = tag.getDouble("followSpeed");
        if (tag.contains("maxLagDistance")) maxLagDistance = tag.getDouble("maxLagDistance");
        if (tag.contains("springMotionEnabled")) springMotionEnabled = tag.getBoolean("springMotionEnabled");
        if (tag.contains("springStiffness")) springStiffness = tag.getDouble("springStiffness");
        if (tag.contains("springDamping")) springDamping = tag.getDouble("springDamping");
        if (tag.contains("rotationFollowSpeed")) rotationFollowSpeed = tag.getDouble("rotationFollowSpeed");
        if (tag.contains("useShortestArc")) useShortestArc = tag.getBoolean("useShortestArc");
        if (tag.contains("orientationFollowSpeed")) orientationFollowSpeed = tag.getDouble("orientationFollowSpeed");
        if (tag.contains("gazeAtViewTarget")) gazeAtViewTarget = tag.getBoolean("gazeAtViewTarget");
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
