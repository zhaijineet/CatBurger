package net.zhaiji.catburger.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.config.FormationGazeTarget;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 一名佩戴者某个槽位上的跟随者运动状态：位置拖尾、环绕偏航、朝向缓冲，以及编队模式下的一条尾巴状态。
 * 状态挂在佩戴者实体上（见 LivingEntityMixin），随实体生灭；读取时按 partialTicks 插值。
 */
public class FollowerState {
    public static final String FORMATION_KEY = "formation";

    // TODO(阶段 7): 目前只按槽位分列状态，仍未遍历渲染全部跟随者与按槽位键播种相位
    private static final double MIN_MOTION_LENGTH = 1.0E-4;
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
    private float gazeYaw;
    private float prevGazeYaw;
    private float targetOrientationYaw;
    private float targetOrientationPitch;
    private float targetGazeYaw;
    private double layoutForward;
    private double layoutRight;
    private double layoutUp;
    private final FollowerTail tail = new FollowerTail();
    private int tailLength;
    private double tailSpacing;
    private double tailDisplaySize;
    private final double floatPhase;
    private final double floatPeriodScale;

    private FollowerState(LivingEntity livingEntity, FollowerRenderData renderParams, String slotKey) {
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
        gazeYaw = prevGazeYaw = 0.0F;
        if (renderParams.isIndependentFloat()) {
            floatPhase = FollowerLayout.floatPhaseOf(livingEntity, slotKey);
            floatPeriodScale = FollowerLayout.floatPeriodScaleOf(livingEntity, slotKey);
        } else {
            floatPhase = 0.0;
            floatPeriodScale = 1.0;
        }
    }

    /**
     * 取该佩戴者某个槽位上的跟随者状态，首次访问时按当前参数建立初始状态；
     * 同时丢弃长期未推进的其他槽位条目，使槽位变化后旧状态不残留。
     */
    public static FollowerState of(LivingEntity livingEntity, String slotKey, FollowerRenderData renderParams) {
        Map<String, FollowerState> slotStates = ((FollowerStateHolder) livingEntity).catburger$followerStates();
        slotStates.entrySet().removeIf(entry -> !entry.getKey().equals(slotKey)
                && livingEntity.tickCount - entry.getValue().lastTick > TICK_GAP_RESET_THRESHOLD);
        return slotStates.computeIfAbsent(slotKey, slot -> new FollowerState(livingEntity, renderParams, slot));
    }

    /**
     * 按佩戴者的 tick 推进状态，同一刻重复调用只生效一次。
     */
    public void update(FollowerRenderData renderParams, double layoutForward, double layoutRight, double layoutUp) {
        this.renderParams = renderParams;
        this.layoutForward = layoutForward;
        this.layoutRight = layoutRight;
        this.layoutUp = layoutUp;
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

        Vec3 gazePosition = gazePositionOf(livingEntity, renderParams);
        updateOrientation(gazePosition, tickGap);
        if (tailLength > 0) updateTail(gazePosition, tickGap);
    }

    /**
     * 编队模式推进：全队共用一条状态，首件之外的各节由尾巴按刻跟随。
     */
    public void updateFormation(FollowerRenderData renderParams, int tailLength, double tailSpacing, double displaySize) {
        this.renderParams = renderParams;
        this.tailLength = tailLength;
        this.tailSpacing = tailSpacing;
        this.tailDisplaySize = displaySize;
        tail.resize(
                tailLength,
                tickPosition(),
                tickBackward(),
                livingEntity.getViewYRot(1.0F),
                livingEntity.getViewXRot(1.0F),
                tailSpacing
        );
        update(renderParams, 0, 0, 0);
    }

    /**
     * 尾巴按刻推进；跳帧或首件被瞬移拉开时整条重排到首件正后方，避免一条长绳从旧位置甩过来。
     */
    private void updateTail(Vec3 gazePosition, boolean snap) {
        Vec3 leaderPosition = tickPosition();
        Vec3 backward = tickBackward();
        double detachDistance = renderParams.getMaxLagDistance() + tailSpacing * (tailLength + 1);
        if (snap || tail.isDetached(leaderPosition, detachDistance)) {
            tail.reset(leaderPosition, backward, targetOrientationYaw, livingEntity.getViewXRot(1.0F), tailSpacing);
            return;
        }

        tail.advance(
                leaderPosition,
                backward,
                tailSpacing,
                renderParams.getFollowSpeed(),
                gazePosition,
                renderParams.getFormationGazeTarget() == FormationGazeTarget.FRONT_FOLLOWER,
                targetOrientationYaw,
                livingEntity.getViewXRot(1.0F),
                (float) renderParams.getOrientationFollowSpeed(),
                livingEntity,
                tailDisplaySize
        );
    }

    /**
     * 本刻首件的世界坐标，取刻末的状态值，注视解算与尾巴推进共用。
     */
    private Vec3 tickPosition() {
        double tickOrbitYaw = pickUsedYaw(springYaw, dragYaw);
        return new Vec3(
                livingEntity.getX() + orbitXOffset(frontBackOffset(), leftRightOffset(), tickOrbitYaw, offsetX),
                livingEntity.getY() + orbitYOffset(renderParams, livingEntity, anchorHeight, offsetY, layoutUp, floatOffset(1.0F)),
                livingEntity.getZ() + orbitZOffset(frontBackOffset(), leftRightOffset(), tickOrbitYaw, offsetZ)
        );
    }

    /**
     * 本刻环绕方向的正后方（水平单位向量），尾巴各节归位、新增与重排时都沿它展开。
     */
    private Vec3 tickBackward() {
        double yawRadians = Math.toRadians(pickUsedYaw(springYaw, dragYaw));
        return new Vec3(Math.sin(yawRadians), 0.0, -Math.cos(yawRadians));
    }

    /**
     * 按 tick 推进朝向缓冲：朝向只受朝向延迟一处缓冲，注视只决定目标朝向。
     */
    private void updateOrientation(Vec3 gazePosition, boolean snap) {
        prevOrientationYaw = orientationYaw;
        prevOrientationPitch = orientationPitch;
        prevGazeYaw = gazeYaw;

        if (!resolveOrientationTarget(gazePosition)) return;

        // 长时间跳帧后直接落到目标，并把上一帧一并拉平，不从过期朝向插值过来
        if (snap) {
            orientationYaw = targetOrientationYaw;
            orientationPitch = targetOrientationPitch;
            gazeYaw = targetGazeYaw;
            prevOrientationYaw = orientationYaw;
            prevOrientationPitch = orientationPitch;
            prevGazeYaw = gazeYaw;
            return;
        }

        float orientationFollowSpeed = (float) renderParams.getOrientationFollowSpeed();
        orientationYaw += Mth.wrapDegrees(targetOrientationYaw - orientationYaw) * orientationFollowSpeed;
        orientationPitch += (targetOrientationPitch - orientationPitch) * orientationFollowSpeed;
        gazeYaw += Mth.wrapDegrees(targetGazeYaw - gazeYaw) * orientationFollowSpeed;
    }

    /**
     * 求本 tick 的目标朝向并写入目标字段，返回 false 表示本刻应保持当前朝向：目标贴脸，或正在背后致使锥面方位角无定义。
     */
    private boolean resolveOrientationTarget(Vec3 gazePosition) {
        float viewYaw = livingEntity.getViewYRot(1.0F);
        float viewPitch = livingEntity.getViewXRot(1.0F);

        Vec3 viewFrameDirection;
        if (gazePosition == null) {
            viewFrameDirection = FollowerOrientation.viewDirectionInViewFrame(viewPitch);
        } else {
            double tickOrbitYaw = pickUsedYaw(springYaw, dragYaw);
            Vec3 toTarget = gazePosition.subtract(
                    livingEntity.getX() + orbitXOffset(frontBackOffset(), leftRightOffset(), tickOrbitYaw, offsetX),
                    livingEntity.getY() + orbitYOffset(renderParams, livingEntity, anchorHeight, offsetY, layoutUp, floatOffset(1.0F)),
                    livingEntity.getZ() + orbitZOffset(frontBackOffset(), leftRightOffset(), tickOrbitYaw, offsetZ)
            );
            if (toTarget.lengthSqr() <= DEGENERATE_GAZE_DISTANCE * DEGENERATE_GAZE_DISTANCE) return false;
            viewFrameDirection = FollowerOrientation.toViewFrame(toTarget, viewYaw);
        }

        Vec3 clampedDirection = FollowerOrientation.clampToCone(viewFrameDirection);
        if (clampedDirection == null) return false;

        targetOrientationYaw = viewYaw;
        targetOrientationPitch = FollowerOrientation.pitchOf(clampedDirection);
        targetGazeYaw = FollowerOrientation.localYawOf(clampedDirection);
        return true;
    }

    /**
     * 取环绕方向上的横向偏移。
     */
    public double xOffset(float partialTicks) {
        return orbitXOffset(
                frontBackOffset(),
                leftRightOffset(),
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
                Mth.lerp(partialTicks, prevAnchorHeight, anchorHeight),
                Mth.lerp(partialTicks, prevOffsetY, offsetY),
                layoutUp,
                floatOffset(partialTicks)
        );
    }

    /**
     * 取环绕方向上的纵向偏移。
     */
    public double zOffset(float partialTicks) {
        return orbitZOffset(
                frontBackOffset(),
                leftRightOffset(),
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
     * 取绕自身竖直轴的局部偏航。
     */
    public float gazeYaw(float partialTicks) {
        return Mth.lerp(partialTicks, prevGazeYaw, gazeYaw);
    }

    /**
     * 取尾巴第 segment 节相对佩戴者渲染原点的横向偏移。
     */
    public double tailXOffset(int segment, float partialTicks) {
        return tail.x(segment, partialTicks) - livingEntity.getPosition(partialTicks).x;
    }

    /**
     * 取尾巴第 segment 节相对佩戴者渲染原点的竖直偏移。
     */
    public double tailYOffset(int segment, float partialTicks) {
        return tail.y(segment, partialTicks) - livingEntity.getPosition(partialTicks).y;
    }

    /**
     * 取尾巴第 segment 节相对佩戴者渲染原点的纵向偏移。
     */
    public double tailZOffset(int segment, float partialTicks) {
        return tail.z(segment, partialTicks) - livingEntity.getPosition(partialTicks).z;
    }

    /**
     * 取尾巴第 segment 节的视线系偏航。
     */
    public float tailYaw(int segment, float partialTicks) {
        return tail.yaw(segment, partialTicks);
    }

    /**
     * 取尾巴第 segment 节的视线系俯仰。
     */
    public float tailPitch(int segment, float partialTicks) {
        return tail.pitch(segment, partialTicks);
    }

    /**
     * 取尾巴第 segment 节注视会聚的局部偏航。
     */
    public float tailGazeYaw(int segment, float partialTicks) {
        return tail.gazeYaw(segment, partialTicks);
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
    public static double previewXOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float viewYaw, double layoutForward, double layoutRight) {
        float entityScale = livingEntity.getScale();
        return orbitXOffset(
                renderParams.getFrontBackOffset() * entityScale + layoutForward,
                renderParams.getLeftRightOffset() * entityScale + layoutRight,
                viewYaw,
                0
        );
    }

    /**
     * 背包等界面预览用的无状态竖直偏移：取实体当前锚点，叠加散布布局，不含拖尾。
     */
    public static double previewYOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float partialTicks, double layoutUp) {
        return orbitYOffset(
                renderParams,
                livingEntity,
                anchorHeightOf(livingEntity, renderParams),
                0,
                layoutUp,
                floatOffsetOf(livingEntity, renderParams, partialTicks)
        );
    }

    /**
     * 背包等界面预览用的无状态纵向偏移：取实体当前朝向，叠加散布布局，不含拖尾。
     */
    public static double previewZOffset(LivingEntity livingEntity, FollowerRenderData renderParams, float viewYaw, double layoutForward, double layoutRight) {
        float entityScale = livingEntity.getScale();
        return orbitZOffset(
                renderParams.getFrontBackOffset() * entityScale + layoutForward,
                renderParams.getLeftRightOffset() * entityScale + layoutRight,
                viewYaw,
                0
        );
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

    private float bodyYaw(float partialTicks) {
        return Mth.lerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
    }
    private double frontBackOffset() {
        return renderParams.getFrontBackOffset() * livingEntity.getScale() + layoutForward;
    }

    private double leftRightOffset() {
        return renderParams.getLeftRightOffset() * livingEntity.getScale() + layoutRight;
    }

    private static double orbitXOffset(double frontBackOffset, double leftRightOffset, double orbitYaw, double dragOffsetX) {
        double yawRadians = Math.toRadians(-orbitYaw);
        return Math.cos(yawRadians - Mth.HALF_PI) * frontBackOffset
                - Math.cos(yawRadians) * leftRightOffset
                + dragOffsetX;
    }

    private static double orbitYOffset(
            FollowerRenderData renderParams,
            LivingEntity livingEntity,
            double anchorHeight,
            double dragOffsetY,
            double layoutUp,
            double floatOffset
    ) {
        float entityScale = livingEntity.getScale();
        return anchorHeight
                + (floatOffset + renderParams.getVerticalOffset()) * entityScale
                + layoutUp
                + dragOffsetY;
    }

    private static double orbitZOffset(double frontBackOffset, double leftRightOffset, double orbitYaw, double dragOffsetZ) {
        double yawRadians = Math.toRadians(-orbitYaw);
        return -Math.sin(yawRadians - Mth.HALF_PI) * frontBackOffset
                + Math.sin(yawRadians) * leftRightOffset
                + dragOffsetZ;
    }

    /**
     * 渲染缩放：参数里的缩放乘以佩戴者自身缩放，使跟随者随佩戴者体型等比缩放。
     */
    public static float renderScale(LivingEntity livingEntity, FollowerRenderData renderParams) {
        return (float) renderParams.getScale() * livingEntity.getScale();
    }

    /**
     * 锚点高度取实体眼高，眼高随姿态与缩放变化，潜行时再叠加参数里的 crouchOffset。
     */
    public static double anchorHeightOf(LivingEntity livingEntity, FollowerRenderData renderParams) {
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
     * 浮动偏移：相位与周期倍率按槽位播种，独立浮动时多件节奏不同。
     */
    private double floatOffset(float partialTicks) {
        return renderParams.getFloatDistance() / 2
                * Math.sin((livingEntity.tickCount + partialTicks) * Mth.TWO_PI
                / (renderParams.getFloatCycleDuration() * floatPeriodScale) + floatPhase);
    }

    /**
     * 浮动偏移：按佩戴者刻数与参数里的浮动距离与周期做往复，预览路径使用。
     */
    private static double floatOffsetOf(LivingEntity livingEntity, FollowerRenderData renderParams, float partialTicks) {
        return renderParams.getFloatDistance() / 2
                * Math.sin((livingEntity.tickCount + partialTicks) * Mth.TWO_PI / renderParams.getFloatCycleDuration());
    }

    /**
     * 解析注视点的世界坐标：摄像机实体直接取本机准心命中点，其他玩家按他自己的眼位、朝向与交互距离在本地重算，
     * 非玩家实体没有准心概念故返回 null 由调用方改用视线方向。
     */
    @Nullable
    private static Vec3 gazePositionOf(LivingEntity livingEntity, FollowerRenderData renderParams) {
        if (!renderParams.isGazeAtViewTarget()) return null;

        Minecraft minecraft = Minecraft.getInstance();
        if (livingEntity == minecraft.getCameraEntity()) {
            HitResult hitResult = minecraft.hitResult;
            return hitResult == null ? null : hitResult.getLocation();
        }

        if (livingEntity instanceof Player player) return replicaGazePosition(player);

        // TODO(其他生物注视): 非玩家实体暂返回 null，由调用方改用它的视线方向；后续要让它们也做命中点会聚，实现方式待定
        return null;
    }

    /**
     * 按游戏内求准心命中点的方式在本地重算：从佩戴者眼位沿其视线做方块与实体射线检测，取更近的那个。
     * 游戏内的交互距离过滤只把超距结果的判定类型改成 MISS、并不移动命中位置，因此这里取位置时无需复现它。
     */
    private static Vec3 replicaGazePosition(Player wearer) {
        double interactionRange = Math.max(wearer.blockInteractionRange(), wearer.entityInteractionRange());
        double entitySearchRangeSqr = Mth.square(interactionRange);
        Vec3 eyePosition = wearer.getEyePosition(1.0F);
        HitResult blockHitResult = wearer.pick(interactionRange, 1.0F, false);
        double blockHitDistanceSqr = blockHitResult.getLocation().distanceToSqr(eyePosition);
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            interactionRange = Math.sqrt(blockHitDistanceSqr);
            entitySearchRangeSqr = blockHitDistanceSqr;
        }

        Vec3 viewVector = wearer.getViewVector(1.0F);
        Vec3 rayEndPosition = eyePosition.add(viewVector.scale(interactionRange));
        AABB searchBox = wearer.getBoundingBox().expandTowards(viewVector.scale(interactionRange)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                wearer,
                eyePosition,
                rayEndPosition,
                searchBox,
                candidate -> !candidate.isSpectator() && candidate.isPickable(),
                entitySearchRangeSqr
        );

        if (entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePosition) < blockHitDistanceSqr)
            return entityHitResult.getLocation();

        return blockHitResult.getLocation();
    }
}
