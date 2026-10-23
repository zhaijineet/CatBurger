package net.zhaiji.catburger.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.DeduplicateMode;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;

/**
 * 一名玩家用于渲染其跟随者的全部参数：客户端按本机配置填好后上传，服务端收束并广播给追踪者。
 */
public class FollowerRenderData implements INBTSerializable<CompoundTag> {
    /**
     * 配置默认值实例，无附件实体回退到它；全局共享只读，任何写入都会污染所有无附件实体。
     */
    public static final FollowerRenderData CONFIG_DEFAULTS = new FollowerRenderData();

    public static final StreamCodec<RegistryFriendlyByteBuf, FollowerRenderData> STREAM_CODEC = StreamCodec.of(
            FollowerRenderData::encode,
            FollowerRenderData::decode
    );

    /**
     * 物品组件用的持久化编解码，复用NBT读写，不必再写一套逐字段的编解码。
     */
    public static final Codec<FollowerRenderData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                FollowerRenderData data = new FollowerRenderData();
                data.deserializeNBT(null, tag);
                return data;
            },
            data -> data.serializeNBT(null)
    );

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
     * 复制一份独立实例，供有主生物持有主人的参数，避免有主生物与主人共享同一个可变实例。
     * 复用NBT读写，新增配置项时不必再维护第二份字段清单。
     */
    public FollowerRenderData copy() {
        FollowerRenderData copiedData = new FollowerRenderData();
        copiedData.deserializeNBT(null, serializeNBT(null));
        return copiedData;
    }

    /**
     * 按NBT比较两份参数是否完全一致，用于跳过没有变化的副本写入与广播。
     * 附件序列化用的provider参数在本类实现里没有被读取，因此直接传null。
     */
    public boolean matches(FollowerRenderData other) {
        return serializeNBT(null).equals(other.serializeNBT(null));
    }

    /**
     * 物品组件要求值类按不可变对象对待，相等实例必须给出相同哈希，因此与matches共用同一份NBT比较。
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        return object instanceof FollowerRenderData other && matches(other);
    }

    @Override
    public int hashCode() {
        return serializeNBT(null).hashCode();
    }

    @Override
    public String toString() {
        return serializeNBT(null).toString();
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
        DeduplicateMode.STREAM_CODEC.encode(buf, data.deduplicateMode);
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
        data.deduplicateMode = DeduplicateMode.STREAM_CODEC.decode(buf);
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
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
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
