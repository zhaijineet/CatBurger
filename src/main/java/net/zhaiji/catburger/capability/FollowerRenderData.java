package net.zhaiji.catburger.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.INBTSerializable;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.DeduplicateMode;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;

/**
 * 一名玩家用于渲染其跟随者的全部参数：客户端按本机配置填好后上传，服务端收束并广播给追踪者。
 */
public class FollowerRenderData implements INBTSerializable<CompoundTag> {
    /**
     * 配置默认值实例，没有同步数据的实体回退到它；全局共享只读，任何写入都会污染所有这类实体。
     */
    public static final FollowerRenderData CONFIG_DEFAULTS = new FollowerRenderData();

    private FollowerRenderMode renderMode = CatBurgerClientConfig.FOLLOWER_RENDER_MODE.getDefault();
    private DeduplicateMode deduplicateMode = CatBurgerClientConfig.DEDUPLICATE_MODE.getDefault();
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

    // 配置卡手动设置过的标记：开启宠物配置同步时跳过这些实体，让卡片设置优先于主人继承
    private boolean manualOverride;

    // 只活在运行期：表示这份参数是否已经由服务端同步过，未同步的实体渲染时应回落到本机配置
    private boolean synced;

    // 只活在运行期：能力实例常驻实体、写入是原地改写，引用不会变，渲染方据此判断内容有没有变过
    private int revision;

    /**
     * 是否已由服务端同步过，1.21.1 由附件的存在性表达同一件事。
     */
    public boolean isSynced() {
        return synced;
    }

    /**
     * 这份参数被写入或重置过多少次，供按引用缓存的结果判断内容是否已经变过。
     */
    public int getRevision() {
        return revision;
    }

    /**
     * 是否被配置卡手动设置过，1.21.1 由独立附件的存在性表达同一件事。
     */
    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }

    /**
     * 按本机客户端配置造一份新实例，供上传与服务端以外的本地回退使用。
     */
    public static FollowerRenderData fromClientConfig() {
        FollowerRenderData data = new FollowerRenderData();
        data.renderMode = CatBurgerClientConfig.FOLLOWER_RENDER_MODE.get();
        data.deduplicateMode = CatBurgerClientConfig.DEDUPLICATE_MODE.get();
        data.independentFloat = CatBurgerClientConfig.INDEPENDENT_FLOAT.get();
        data.minSpacing = CatBurgerClientConfig.MIN_SPACING.get();
        data.maxSpacing = CatBurgerClientConfig.MAX_SPACING.get();
        data.maxRadius = CatBurgerClientConfig.MAX_RADIUS.get();
        data.formationSpacing = CatBurgerClientConfig.FORMATION_SPACING.get();
        data.formationMaxLagDistance = CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE.get();
        data.formationGazeTarget = CatBurgerClientConfig.FORMATION_GAZE_TARGET.get();
        data.scale = CatBurgerClientConfig.RENDER_SCALE.get();
        data.frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.get();
        data.leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.get();
        data.verticalOffset = CatBurgerClientConfig.VERTICAL_OFFSET.get();
        data.crouchOffset = CatBurgerClientConfig.CROUCH_OFFSET.get();
        data.floatDistance = CatBurgerClientConfig.FLOAT_DISTANCE.get();
        data.floatCycleDuration = CatBurgerClientConfig.FLOAT_CYCLE_DURATION.get();
        data.followSpeed = CatBurgerClientConfig.FOLLOW_SPEED.get();
        data.maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.get();
        data.springMotionEnabled = CatBurgerClientConfig.SPRING_MOTION_ENABLED.get();
        data.springStiffness = CatBurgerClientConfig.SPRING_STIFFNESS.get();
        data.springDamping = CatBurgerClientConfig.SPRING_DAMPING.get();
        data.rotationFollowSpeed = CatBurgerClientConfig.ROTATION_FOLLOW_SPEED.get();
        data.useShortestArc = CatBurgerClientConfig.USE_SHORTEST_ARC.get();
        data.orientationFollowSpeed = CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED.get();
        data.gazeAtViewTarget = CatBurgerClientConfig.GAZE_AT_VIEW_TARGET.get();
        return data;
    }

    /**
     * 按NBT比较两份参数是否完全一致，用于跳过没有变化的副本写入与广播。
     */
    public boolean matches(FollowerRenderData other) {
        return serializeNBT().equals(other.serializeNBT());
    }

    /**
     * 把全部渲染参数字段逐项恢复成配置默认值，并标记为未同步。能力实例常驻实体，没有「移除」这一动作，改用重置代替。
     * 手动设置标记不属于渲染参数，由配置卡的移除模式单独清除。
     */
    public void markUnsynced() {
        renderMode = CatBurgerClientConfig.FOLLOWER_RENDER_MODE.getDefault();
        deduplicateMode = CatBurgerClientConfig.DEDUPLICATE_MODE.getDefault();
        independentFloat = CatBurgerClientConfig.INDEPENDENT_FLOAT.getDefault();
        minSpacing = CatBurgerClientConfig.MIN_SPACING.getDefault();
        maxSpacing = CatBurgerClientConfig.MAX_SPACING.getDefault();
        maxRadius = CatBurgerClientConfig.MAX_RADIUS.getDefault();
        formationSpacing = CatBurgerClientConfig.FORMATION_SPACING.getDefault();
        formationMaxLagDistance = CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE.getDefault();
        formationGazeTarget = CatBurgerClientConfig.FORMATION_GAZE_TARGET.getDefault();
        scale = CatBurgerClientConfig.RENDER_SCALE.getDefault();
        frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.getDefault();
        leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.getDefault();
        verticalOffset = CatBurgerClientConfig.VERTICAL_OFFSET.getDefault();
        crouchOffset = CatBurgerClientConfig.CROUCH_OFFSET.getDefault();
        floatDistance = CatBurgerClientConfig.FLOAT_DISTANCE.getDefault();
        floatCycleDuration = CatBurgerClientConfig.FLOAT_CYCLE_DURATION.getDefault();
        followSpeed = CatBurgerClientConfig.FOLLOW_SPEED.getDefault();
        maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.getDefault();
        springMotionEnabled = CatBurgerClientConfig.SPRING_MOTION_ENABLED.getDefault();
        springStiffness = CatBurgerClientConfig.SPRING_STIFFNESS.getDefault();
        springDamping = CatBurgerClientConfig.SPRING_DAMPING.getDefault();
        rotationFollowSpeed = CatBurgerClientConfig.ROTATION_FOLLOW_SPEED.getDefault();
        useShortestArc = CatBurgerClientConfig.USE_SHORTEST_ARC.getDefault();
        orientationFollowSpeed = CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED.getDefault();
        gazeAtViewTarget = CatBurgerClientConfig.GAZE_AT_VIEW_TARGET.getDefault();
        synced = false;
        revision++;
    }

    /**
     * 把各数值字段收束到配置声明的边界内，用于校验跨端传入的值。
     */
    public void clampToBounds() {
        minSpacing = clampToConfig(CatBurgerClientConfig.MIN_SPACING, minSpacing, CatBurgerClientConfig.MIN_SPACING_LOWER, CatBurgerClientConfig.MIN_SPACING_UPPER);
        maxSpacing = clampToConfig(CatBurgerClientConfig.MAX_SPACING, maxSpacing, CatBurgerClientConfig.MAX_SPACING_LOWER, CatBurgerClientConfig.MAX_SPACING_UPPER);
        maxRadius = clampToConfig(CatBurgerClientConfig.MAX_RADIUS, maxRadius, CatBurgerClientConfig.MAX_RADIUS_LOWER, CatBurgerClientConfig.MAX_RADIUS_UPPER);
        formationSpacing = clampToConfig(CatBurgerClientConfig.FORMATION_SPACING, formationSpacing, CatBurgerClientConfig.FORMATION_SPACING_LOWER, CatBurgerClientConfig.FORMATION_SPACING_UPPER);
        formationMaxLagDistance = clampToConfig(CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE, formationMaxLagDistance, CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE_LOWER, CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE_UPPER);
        scale = clampToConfig(CatBurgerClientConfig.RENDER_SCALE, scale, CatBurgerClientConfig.RENDER_SCALE_LOWER, CatBurgerClientConfig.RENDER_SCALE_UPPER);
        frontBackOffset = clampToConfig(CatBurgerClientConfig.FRONT_BACK_OFFSET, frontBackOffset, CatBurgerClientConfig.FRONT_BACK_OFFSET_LOWER, CatBurgerClientConfig.FRONT_BACK_OFFSET_UPPER);
        leftRightOffset = clampToConfig(CatBurgerClientConfig.LEFT_RIGHT_OFFSET, leftRightOffset, CatBurgerClientConfig.LEFT_RIGHT_OFFSET_LOWER, CatBurgerClientConfig.LEFT_RIGHT_OFFSET_UPPER);
        verticalOffset = clampToConfig(CatBurgerClientConfig.VERTICAL_OFFSET, verticalOffset, CatBurgerClientConfig.VERTICAL_OFFSET_LOWER, CatBurgerClientConfig.VERTICAL_OFFSET_UPPER);
        crouchOffset = clampToConfig(CatBurgerClientConfig.CROUCH_OFFSET, crouchOffset, CatBurgerClientConfig.CROUCH_OFFSET_LOWER, CatBurgerClientConfig.CROUCH_OFFSET_UPPER);
        floatDistance = clampToConfig(CatBurgerClientConfig.FLOAT_DISTANCE, floatDistance, CatBurgerClientConfig.FLOAT_DISTANCE_LOWER, CatBurgerClientConfig.FLOAT_DISTANCE_UPPER);
        floatCycleDuration = clampToConfig(CatBurgerClientConfig.FLOAT_CYCLE_DURATION, floatCycleDuration, CatBurgerClientConfig.FLOAT_CYCLE_DURATION_LOWER, CatBurgerClientConfig.FLOAT_CYCLE_DURATION_UPPER);
        followSpeed = clampToConfig(CatBurgerClientConfig.FOLLOW_SPEED, followSpeed, CatBurgerClientConfig.FOLLOW_SPEED_LOWER, CatBurgerClientConfig.FOLLOW_SPEED_UPPER);
        maxLagDistance = clampToConfig(CatBurgerClientConfig.MAX_LAG_DISTANCE, maxLagDistance, CatBurgerClientConfig.MAX_LAG_DISTANCE_LOWER, CatBurgerClientConfig.MAX_LAG_DISTANCE_UPPER);
        springStiffness = clampToConfig(CatBurgerClientConfig.SPRING_STIFFNESS, springStiffness, CatBurgerClientConfig.SPRING_STIFFNESS_LOWER, CatBurgerClientConfig.SPRING_STIFFNESS_UPPER);
        springDamping = clampToConfig(CatBurgerClientConfig.SPRING_DAMPING, springDamping, CatBurgerClientConfig.SPRING_DAMPING_LOWER, CatBurgerClientConfig.SPRING_DAMPING_UPPER);
        rotationFollowSpeed = clampToConfig(CatBurgerClientConfig.ROTATION_FOLLOW_SPEED, rotationFollowSpeed, CatBurgerClientConfig.ROTATION_FOLLOW_SPEED_LOWER, CatBurgerClientConfig.ROTATION_FOLLOW_SPEED_UPPER);
        orientationFollowSpeed = clampToConfig(CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED, orientationFollowSpeed, CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED_LOWER, CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED_UPPER);
    }

    /**
     * 收束到配置声明的边界内，非有限值回落到配置默认值，避免 NaN 与 Infinity 进入渲染矩阵。
     */
    private static double clampToConfig(ForgeConfigSpec.DoubleValue value, double rawValue, double lowerBound, double upperBound) {
        if (!Double.isFinite(rawValue)) return value.getDefault();
        return Mth.clamp(rawValue, lowerBound, upperBound);
    }

    /**
     * 按字段顺序写入网络缓冲，两端只依赖这里与decode的顺序一致。
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(renderMode);
        buf.writeEnum(deduplicateMode);
        buf.writeBoolean(independentFloat);
        buf.writeDouble(minSpacing);
        buf.writeDouble(maxSpacing);
        buf.writeDouble(maxRadius);
        buf.writeDouble(formationSpacing);
        buf.writeDouble(formationMaxLagDistance);
        buf.writeEnum(formationGazeTarget);
        buf.writeDouble(scale);
        buf.writeDouble(frontBackOffset);
        buf.writeDouble(leftRightOffset);
        buf.writeDouble(verticalOffset);
        buf.writeDouble(crouchOffset);
        buf.writeDouble(floatDistance);
        buf.writeDouble(floatCycleDuration);
        buf.writeDouble(followSpeed);
        buf.writeDouble(maxLagDistance);
        buf.writeBoolean(springMotionEnabled);
        buf.writeDouble(springStiffness);
        buf.writeDouble(springDamping);
        buf.writeDouble(rotationFollowSpeed);
        buf.writeBoolean(useShortestArc);
        buf.writeDouble(orientationFollowSpeed);
        buf.writeBoolean(gazeAtViewTarget);
    }

    public static FollowerRenderData decode(FriendlyByteBuf buf) {
        FollowerRenderData data = new FollowerRenderData();
        data.renderMode = buf.readEnum(FollowerRenderMode.class);
        data.deduplicateMode = buf.readEnum(DeduplicateMode.class);
        data.independentFloat = buf.readBoolean();
        data.minSpacing = buf.readDouble();
        data.maxSpacing = buf.readDouble();
        data.maxRadius = buf.readDouble();
        data.formationSpacing = buf.readDouble();
        data.formationMaxLagDistance = buf.readDouble();
        data.formationGazeTarget = buf.readEnum(FormationGazeTarget.class);
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
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("renderMode", renderMode.getSerializedName());
        tag.putString("deduplicateMode", deduplicateMode.getSerializedName());
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
        tag.putBoolean("manualOverride", manualOverride);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // 空标签表示服务端没有存过参数，保持未同步，渲染方据此回落到本机配置
        if (tag.isEmpty()) return;
        synced = true;
        if (tag.contains("renderMode")) renderMode = FollowerRenderMode.byName(tag.getString("renderMode"));
        if (tag.contains("deduplicateMode")) deduplicateMode = DeduplicateMode.byName(tag.getString("deduplicateMode"));
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
        if (tag.contains("manualOverride")) manualOverride = tag.getBoolean("manualOverride");
        revision++;
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

    public DeduplicateMode getDeduplicateMode() {
        return deduplicateMode;
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
