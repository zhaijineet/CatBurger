package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zhaiji.catburger.attachment.FollowerRenderData;

import java.util.Map;

/**
 * 一名佩戴者某个槽位上的跟随者运动状态：位置拖尾、环绕偏航、朝向缓冲与屏幕扶正滚转。
 * 状态挂在佩戴者实体上（见 LivingEntityMixin），随实体生灭；读取时按 partialTicks 插值。
 */
public class FollowerState {
    // TODO(阶段 7): 目前只按槽位分列状态，仍未遍历渲染全部跟随者与按槽位键播种相位
    private static final double MIN_MOTION_LENGTH = 1.0E-4;
    private static final double MIN_GAZE_HORIZONTAL_DISTANCE = 0.5;
    private static final double DEGENERATE_GAZE_DISTANCE = 0.1;
    // 与跳帧复位同一阈值：超过该刻数未推进的槽位状态视为失效
    private static final int TICK_GAP_RESET_THRESHOLD = 5;

    private final LivingEntity livingEntity;
    private FollowerRenderData renderParams;

    private int lastTick;
    private double lastEntityX;
    private double lastEntityY;
    private double lastEntityZ;
    private float lastHeadYaw;
    private float targetYaw;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private double prevOffsetX;
    private double prevOffsetY;
    private double prevOffsetZ;
    private double anchorHeight;
    private double prevAnchorHeight;
    private float dragYaw;
    private float prevDragYaw;
    private float springYaw;
    private float prevSpringYaw;
    private float springVelocity;
    private float orientationYaw;
    private float prevOrientationYaw;
    private float orientationPitch;
    private float prevOrientationPitch;
    private float orientationRoll;
    private float prevOrientationRoll;
    private float targetOrientationYaw;
    private float targetOrientationPitch;
    private float targetOrientationRoll;

    private FollowerState(LivingEntity livingEntity, FollowerRenderData renderParams) {
        this.livingEntity = livingEntity;
        this.renderParams = renderParams;
        lastTick = livingEntity.tickCount;
        lastEntityX = livingEntity.getX();
        lastEntityY = livingEntity.getY();
        lastEntityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        lastHeadYaw = targetYaw = dragYaw = prevDragYaw = springYaw = prevSpringYaw = headYaw;
        anchorHeight = prevAnchorHeight = anchorHeightOf(livingEntity, renderParams);
        orientationYaw = prevOrientationYaw = headYaw;
        orientationPitch = prevOrientationPitch = livingEntity.getViewXRot(1.0F);
        orientationRoll = prevOrientationRoll = 0.0F;
    }

    /**
     * 取该佩戴者某个槽位上的跟随者状态，首次访问时按当前参数建立初始状态；
     * 同时丢弃长期未推进的其他槽位条目，使槽位变化后旧状态不残留。
     */
    public static FollowerState of(LivingEntity livingEntity, String slotKey, FollowerRenderData renderParams) {
        Map<String, FollowerState> slotStates = ((FollowerStateHolder) livingEntity).catburger$followerStates();
        slotStates.entrySet().removeIf(entry -> !entry.getKey().equals(slotKey)
                && livingEntity.tickCount - entry.getValue().lastTick > TICK_GAP_RESET_THRESHOLD);
        return slotStates.computeIfAbsent(slotKey, slot -> new FollowerState(livingEntity, renderParams));
    }

    /**
     * 按佩戴者的 tick 推进状态，同一刻重复调用只生效一次。
     */
    public void update(FollowerRenderData renderParams) {
        this.renderParams = renderParams;
        int currentTick = livingEntity.tickCount;
        if (currentTick == lastTick) return;

        double entityX = livingEntity.getX();
        double entityY = livingEntity.getY();
        double entityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        double targetAnchorHeight = anchorHeightOf(livingEntity, renderParams);

        boolean tickGap = currentTick - lastTick > TICK_GAP_RESET_THRESHOLD;
        if (tickGap) {
            lastEntityX = entityX;
            lastEntityY = entityY;
            lastEntityZ = entityZ;
            lastHeadYaw = headYaw;
            targetYaw = headYaw;
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
            anchorHeight = targetAnchorHeight;
            dragYaw = headYaw;
            springYaw = headYaw;
            springVelocity = 0;
        }

        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
        prevOffsetZ = offsetZ;
        prevAnchorHeight = anchorHeight;
        prevDragYaw = dragYaw;
        prevSpringYaw = springYaw;

        targetYaw += Mth.wrapDegrees(headYaw - lastHeadYaw);

        double followSpeed = renderParams.getFollowSpeed();
        double motionX = entityX - lastEntityX;
        double motionY = entityY - lastEntityY;
        double motionZ = entityZ - lastEntityZ;
        double motionLength = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        double targetOffsetX = 0;
        double targetOffsetY = 0;
        double targetOffsetZ = 0;
        if (motionLength > MIN_MOTION_LENGTH) {
            double lagDistance = lagDistance(followSpeed, motionLength);
            targetOffsetX = -motionX / motionLength * lagDistance;
            targetOffsetY = -motionY / motionLength * lagDistance;
            targetOffsetZ = -motionZ / motionLength * lagDistance;
        }

        offsetX += (targetOffsetX - offsetX) * followSpeed;
        offsetY += (targetOffsetY - offsetY) * followSpeed;
        offsetZ += (targetOffsetZ - offsetZ) * followSpeed;
        anchorHeight += (targetAnchorHeight - anchorHeight) * followSpeed;

        float angleDiff = renderParams.isUseShortestArc()
                ? Mth.wrapDegrees(targetYaw - dragYaw)
                : targetYaw - dragYaw;
        dragYaw += angleDiff * (float) renderParams.getRotationFollowSpeed();

        if (renderParams.isSpringMotionEnabled()) {
            float displacement = targetYaw - springYaw;
            float springForce = (float) renderParams.getSpringStiffness() * displacement;
            springVelocity = springVelocity * (float) renderParams.getSpringDamping() + springForce;
            springYaw += springVelocity;
        } else {
            springYaw = targetYaw;
            springVelocity = 0;
        }

        lastEntityX = entityX;
        lastEntityY = entityY;
        lastEntityZ = entityZ;
        lastHeadYaw = headYaw;
        lastTick = currentTick;

        updateOrientation(tickGap);
    }

    /**
     * 按 tick 推进朝向缓冲：朝向只受朝向延迟一处缓冲，注视只决定目标朝向。
     */
    private void updateOrientation(boolean snap) {
        prevOrientationYaw = orientationYaw;
        prevOrientationPitch = orientationPitch;
        prevOrientationRoll = orientationRoll;

        if (!resolveOrientationTarget()) return;

        // 长时间跳帧后直接落到目标，并把上一帧一并拉平，不从过期朝向插值过来
        if (snap) {
            orientationYaw = targetOrientationYaw;
            orientationPitch = targetOrientationPitch;
            orientationRoll = targetOrientationRoll;
            prevOrientationYaw = orientationYaw;
            prevOrientationPitch = orientationPitch;
            prevOrientationRoll = orientationRoll;
            return;
        }

        float orientationFollowSpeed = (float) renderParams.getOrientationFollowSpeed();
        orientationYaw += Mth.wrapDegrees(targetOrientationYaw - orientationYaw) * orientationFollowSpeed;
        orientationPitch += (targetOrientationPitch - orientationPitch) * orientationFollowSpeed;
        orientationRoll += Mth.wrapDegrees(targetOrientationRoll - orientationRoll) * orientationFollowSpeed;
    }

    /**
     * 求本 tick 的目标朝向并写入目标字段，返回 false 表示注视点与跟随者几乎重合、应保持当前朝向。
     * 水平分量过小时沿用当前偏航，俯仰与滚转照常更新。
     */
    private boolean resolveOrientationTarget() {
        Vec3 gazePosition = GazeResolver.gazePositionOf(livingEntity, renderParams);
        // 无注视目标时朝向取视线方向，滚转补偿照常算，使抬头低头的扶正不依赖注视开关
        if (gazePosition == null) {
            targetOrientationYaw = livingEntity.getViewYRot(1.0F);
            targetOrientationPitch = livingEntity.getViewXRot(1.0F);
            Vec3 viewForward = Vec3.directionFromRotation(targetOrientationPitch, targetOrientationYaw);
            targetOrientationRoll = GazeResolver.screenRoll(viewForward);
            return true;
        }

        double tickOrbitYaw = pickUsedYaw(springYaw, dragYaw);
        Vec3 toTarget = gazePosition.subtract(
                livingEntity.getX() + orbitXOffset(renderParams, livingEntity.getScale(), tickOrbitYaw, offsetX),
                livingEntity.getY() + orbitYOffset(renderParams, livingEntity, 1.0F, anchorHeight, offsetY),
                livingEntity.getZ() + orbitZOffset(renderParams, livingEntity.getScale(), tickOrbitYaw, offsetZ)
        );
        if (toTarget.lengthSqr() <= DEGENERATE_GAZE_DISTANCE * DEGENERATE_GAZE_DISTANCE) return false;

        double horizontalDistance = toTarget.horizontalDistance();
        targetOrientationYaw = horizontalDistance <= MIN_GAZE_HORIZONTAL_DISTANCE
                ? orientationYaw
                : (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        targetOrientationPitch = (float) Math.toDegrees(Math.atan2(-toTarget.y, horizontalDistance));
        Vec3 forward = Vec3.directionFromRotation(targetOrientationPitch, targetOrientationYaw);
        targetOrientationRoll = GazeResolver.screenRoll(forward);
        return true;
    }

    /**
     * 取环绕方向上的横向偏移。
     */
    public double xOffset(float partialTicks) {
        return orbitXOffset(
                renderParams,
                livingEntity.getScale(),
                orbitYaw(partialTicks),
                Mth.lerp(partialTicks, prevOffsetX, offsetX)
        );
    }

    /**
     * 取相对佩戴者脚部原点的竖直偏移。
     */
    public double yOffset(float partialTicks) {
        return orbitYOffset(
                renderParams,
                livingEntity,
                partialTicks,
                Mth.lerp(partialTicks, prevAnchorHeight, anchorHeight),
                Mth.lerp(partialTicks, prevOffsetY, offsetY)
        );
    }

    /**
     * 取环绕方向上的纵向偏移。
     */
    public double zOffset(float partialTicks) {
        return orbitZOffset(
                renderParams,
                livingEntity.getScale(),
                orbitYaw(partialTicks),
                Mth.lerp(partialTicks, prevOffsetZ, offsetZ)
        );
    }

    /**
     * 取朝向偏航。
     */
    public float renderYaw(float partialTicks) {
        return Mth.lerp(partialTicks, prevOrientationYaw, orientationYaw);
    }

    /**
     * 取朝向俯仰。
     */
    public float headPitch(float partialTicks) {
        return Mth.lerp(partialTicks, prevOrientationPitch, orientationPitch);
    }

    /**
     * 取屏幕扶正滚转，由本机相机决定。
     */
    public float screenRoll(float partialTicks) {
        return Mth.lerp(partialTicks, prevOrientationRoll, orientationRoll);
    }

    /**
     * 取渲染缩放。
     */
    public float scale() {
        return renderScale(livingEntity, renderParams);
    }

    /**
     * 背包等界面预览用的无状态横向偏移：取实体当前朝向，不含拖尾。
     */
    public static double previewXOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float viewYaw) {
        return orbitXOffset(renderParams, livingEntity.getScale(), viewYaw, 0);
    }

    /**
     * 背包等界面预览用的无状态竖直偏移：取实体当前锚点，不含拖尾。
     */
    public static double previewYOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float partialTicks) {
        return orbitYOffset(renderParams, livingEntity, partialTicks, anchorHeightOf(livingEntity, renderParams), 0);
    }

    /**
     * 背包等界面预览用的无状态纵向偏移：取实体当前朝向，不含拖尾。
     */
    public static double previewZOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float viewYaw) {
        return orbitZOffset(renderParams, livingEntity.getScale(), viewYaw, 0);
    }

    /**
     * 背包等界面预览用的无状态缩放。
     */
    public static float previewScale(LivingEntity livingEntity, FollowerRenderData renderParams) {
        return renderScale(livingEntity, renderParams);
    }

    /**
     * 取环绕偏航：弹簧运动优先，否则用转向延迟。
     */
    private float pickUsedYaw(float springYaw, float dragYaw) {
        if (renderParams.isSpringMotionEnabled()) return springYaw;
        return dragYaw;
    }

    private double orbitYaw(float partialTicks) {
        return pickUsedYaw(
                Mth.lerp(partialTicks, prevSpringYaw, springYaw),
                Mth.lerp(partialTicks, prevDragYaw, dragYaw)
        );
    }

    private static double orbitXOffset(FollowerRenderData renderParams, float entityScale, double orbitYaw, double dragOffsetX) {
        double frontBackOffset = renderParams.getFrontBackOffset() * entityScale;
        double leftRightOffset = renderParams.getLeftRightOffset() * entityScale;
        double yawRadians = Math.toRadians(-orbitYaw);
        return Math.cos(yawRadians - Mth.HALF_PI) * frontBackOffset
                - Math.cos(yawRadians) * leftRightOffset
                + dragOffsetX;
    }

    private static double orbitYOffset(
            FollowerRenderData renderParams,
            LivingEntity livingEntity,
            float partialTicks,
            double anchorHeight,
            double dragOffsetY
    ) {
        float entityScale = livingEntity.getScale();
        return anchorHeight
                + (floatOffsetOf(livingEntity, renderParams, partialTicks) + renderParams.getVerticalOffset()) * entityScale
                + dragOffsetY;
    }

    private static double orbitZOffset(FollowerRenderData renderParams, float entityScale, double orbitYaw, double dragOffsetZ) {
        double frontBackOffset = renderParams.getFrontBackOffset() * entityScale;
        double leftRightOffset = renderParams.getLeftRightOffset() * entityScale;
        double yawRadians = Math.toRadians(-orbitYaw);
        return -Math.sin(yawRadians - Mth.HALF_PI) * frontBackOffset
                + Math.sin(yawRadians) * leftRightOffset
                + dragOffsetZ;
    }

    /**
     * 渲染缩放：参数里的缩放乘以佩戴者自身缩放，使跟随者随佩戴者体型等比缩放。
     */
    private static float renderScale(LivingEntity livingEntity, FollowerRenderData renderParams) {
        return (float) renderParams.getScale() * livingEntity.getScale();
    }

    /**
     * 锚点高度取实体眼高，眼高随姿态与缩放变化，潜行时再叠加参数里的 crouchOffset。
     */
    private static double anchorHeightOf(LivingEntity livingEntity, FollowerRenderData renderParams) {
        double eyeHeight = livingEntity.getEyeHeight();
        if (!livingEntity.isCrouching()) return eyeHeight;
        return eyeHeight + renderParams.getCrouchOffset() * livingEntity.getScale();
    }

    /**
     * 有界滞后距离：低速段斜率取位置追随的稳态滞后比 (1 - followSpeed) / followSpeed，高速段平滑饱和到最大滞后距离。
     */
    private double lagDistance(double followSpeed, double motionLength) {
        double maxLagDistance = renderParams.getMaxLagDistance();
        if (maxLagDistance <= 0) return 0;
        double lagSpeedRatio = (1 - followSpeed) / followSpeed;
        return maxLagDistance * Math.tanh(lagSpeedRatio * motionLength / maxLagDistance);
    }

    /**
     * 浮动偏移：按佩戴者刻数与参数里的浮动距离与周期做往复。
     */
    private static double floatOffsetOf(LivingEntity livingEntity, FollowerRenderData renderParams, float partialTicks) {
        return renderParams.getFloatDistance() / 2
                * Math.sin((livingEntity.tickCount + partialTicks) * Mth.TWO_PI / renderParams.getFloatCycleDuration());
    }
}
