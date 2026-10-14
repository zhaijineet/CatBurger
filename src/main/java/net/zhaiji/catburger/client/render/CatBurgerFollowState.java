package net.zhaiji.catburger.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class CatBurgerFollowState {
    // TODO(阶段 6.2): 过渡实现，每个实体只有一份状态；改为以实体为外层弱键、槽位键为内层的两层结构（D1），并按槽位键播种相位（D3）
    private static final Map<LivingEntity, CatBurgerFollowState> STATES = new WeakHashMap<>();

    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final double MIN_MOTION_LENGTH = 1.0E-4;
    private static final double MIN_GAZE_DISTANCE = 0.5;
    private static final double MIN_GAZE_OVERLAP_DISTANCE = 0.1;
    private static final double MIN_GAZE_ALIGNMENT = 1.0E-4;
    private static final double GAZE_ROLL_FADE_ALIGNMENT = 0.2;
    private static final int TICK_GAP_RESET_THRESHOLD = 5;

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

    private CatBurgerFollowState(LivingEntity livingEntity) {
        lastTick = livingEntity.tickCount;
        lastEntityX = livingEntity.getX();
        lastEntityY = livingEntity.getY();
        lastEntityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        lastHeadYaw = targetYaw = dragYaw = prevDragYaw = springYaw = prevSpringYaw = headYaw;
        anchorHeight = prevAnchorHeight = getTargetAnchorHeight(livingEntity);
        orientationYaw = prevOrientationYaw = headYaw;
        orientationPitch = prevOrientationPitch = livingEntity.getViewXRot(1.0F);
        orientationRoll = prevOrientationRoll = 0.0F;
    }

    public static CatBurgerFollowState of(LivingEntity livingEntity) {
        return STATES.computeIfAbsent(livingEntity, CatBurgerFollowState::new);
    }

    public void update(LivingEntity livingEntity) {
        int currentTick = livingEntity.tickCount;
        if (currentTick == lastTick) return;

        double entityX = livingEntity.getX();
        double entityY = livingEntity.getY();
        double entityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        double targetAnchorHeight = getTargetAnchorHeight(livingEntity);

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

        double followSpeed = CatBurgerClientConfig.FOLLOW_SPEED.get();
        double motionX = entityX - lastEntityX;
        double motionY = entityY - lastEntityY;
        double motionZ = entityZ - lastEntityZ;
        double motionLength = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        double targetOffsetX = 0;
        double targetOffsetY = 0;
        double targetOffsetZ = 0;
        if (motionLength > MIN_MOTION_LENGTH) {
            double lagDistance = getLagDistance(followSpeed, motionLength);
            targetOffsetX = -motionX / motionLength * lagDistance;
            targetOffsetY = -motionY / motionLength * lagDistance;
            targetOffsetZ = -motionZ / motionLength * lagDistance;
        }

        offsetX += (targetOffsetX - offsetX) * followSpeed;
        offsetY += (targetOffsetY - offsetY) * followSpeed;
        offsetZ += (targetOffsetZ - offsetZ) * followSpeed;
        anchorHeight += (targetAnchorHeight - anchorHeight) * followSpeed;

        float angleDiff = CatBurgerClientConfig.USE_SHORTEST_ARC.get()
                ? Mth.wrapDegrees(targetYaw - dragYaw)
                : targetYaw - dragYaw;
        dragYaw += angleDiff * CatBurgerClientConfig.ROTATION_FOLLOW_SPEED.get().floatValue();

        if (CatBurgerClientConfig.SPRING_MOTION_ENABLED.get()) {
            float displacement = targetYaw - springYaw;
            float springForce = CatBurgerClientConfig.STIFFNESS.get().floatValue() * displacement;
            springVelocity = springVelocity * CatBurgerClientConfig.DAMPING.get().floatValue() + springForce;
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

        updateOrientation(livingEntity, tickGap);
    }

    /**
     * 按 tick 推进朝向缓冲：朝向只受朝向延迟一处缓冲，注视只决定目标朝向。
     */
    private void updateOrientation(LivingEntity livingEntity, boolean snap) {
        prevOrientationYaw = orientationYaw;
        prevOrientationPitch = orientationPitch;
        prevOrientationRoll = orientationRoll;

        Orientation target = resolveOrientationTarget(livingEntity);
        if (target == null) return;

        float orientationFollowSpeed = snap
                ? 1.0F
                : CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED.get().floatValue();
        orientationYaw += Mth.wrapDegrees(target.yaw() - orientationYaw) * orientationFollowSpeed;
        orientationPitch += (target.pitch() - orientationPitch) * orientationFollowSpeed;
        orientationRoll += Mth.wrapDegrees(target.roll() - orientationRoll) * orientationFollowSpeed;
        if (snap) {
            // 长时间跳帧后直接落到目标，不从过期朝向插值过来
            prevOrientationYaw = orientationYaw;
            prevOrientationPitch = orientationPitch;
            prevOrientationRoll = orientationRoll;
        }
    }

    /**
     * 取环绕偏航：弹簧运动优先，否则用转向延迟。
     */
    private static float pickUsedYaw(float springYaw, float dragYaw) {
        if (CatBurgerClientConfig.SPRING_MOTION_ENABLED.get()) return springYaw;
        return dragYaw;
    }

    public CatBurgerFollowTransform resolve(LivingEntity livingEntity, float partialTicks) {
        float usedYaw = pickUsedYaw(
                Mth.lerp(partialTicks, prevSpringYaw, springYaw),
                Mth.lerp(partialTicks, prevDragYaw, dragYaw)
        );

        return composeTransform(
                livingEntity,
                partialTicks,
                usedYaw,
                Mth.lerp(partialTicks, prevOffsetX, offsetX),
                Mth.lerp(partialTicks, prevOffsetY, offsetY),
                Mth.lerp(partialTicks, prevOffsetZ, offsetZ),
                Mth.lerp(partialTicks, prevAnchorHeight, anchorHeight)
        ).withOrientation(
                Mth.lerp(partialTicks, prevOrientationYaw, orientationYaw),
                Mth.lerp(partialTicks, prevOrientationPitch, orientationPitch),
                Mth.lerp(partialTicks, prevOrientationRoll, orientationRoll)
        );
    }

    /**
     * 求本 tick 的目标朝向：有注视目标时朝向注视点，否则取视线方向。
     * 注视点与汉堡几乎重合时返回 null 表示保持当前朝向；水平分量过小时沿用当前偏航，俯仰与滚转照常更新。
     */
    @Nullable
    private Orientation resolveOrientationTarget(LivingEntity livingEntity) {
        GazeTarget gazeTarget = resolveGazeTarget(livingEntity);
        if (gazeTarget == null) return new Orientation(livingEntity.getViewYRot(1.0F), livingEntity.getViewXRot(1.0F), 0.0F);

        CatBurgerFollowTransform tickTransform = composeTransform(
                livingEntity,
                1.0F,
                pickUsedYaw(springYaw, dragYaw),
                offsetX,
                offsetY,
                offsetZ,
                anchorHeight
        );
        Vec3 toTarget = gazeTarget.position().subtract(
                livingEntity.getX() + tickTransform.xOffset(),
                livingEntity.getY() + tickTransform.yOffset(),
                livingEntity.getZ() + tickTransform.zOffset()
        );
        if (toTarget.lengthSqr() <= MIN_GAZE_OVERLAP_DISTANCE * MIN_GAZE_OVERLAP_DISTANCE) return null;

        double horizontalDistance = toTarget.horizontalDistance();
        float yaw = horizontalDistance <= MIN_GAZE_DISTANCE
                ? orientationYaw
                : (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-toTarget.y, horizontalDistance));
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
        return new Orientation(yaw, pitch, computeGazeRoll(forward, gazeTarget.cameraUp(), gazeTarget.cameraForward()));
    }

    /**
     * 求绕前向轴的补偿滚转：把汉堡 up 从无滚转方向转到"投影到相机屏幕后与相机 up 重合"的方向。
     * 前向接近垂直于相机视线时投影会翻转，补偿量随 |cameraForward·forward| 线性淡出到 0，避免滚转翻转 180°。
     */
    private static float computeGazeRoll(Vec3 forward, Vec3 cameraUp, Vec3 cameraForward) {
        double alignment = cameraForward.dot(forward);
        if (Math.abs(alignment) < MIN_GAZE_ALIGNMENT) return 0.0F;

        Vec3 noRollUp = WORLD_UP.subtract(forward.scale(WORLD_UP.dot(forward))).normalize();
        Vec3 screenUp = cameraUp.subtract(cameraForward.scale(cameraUp.dot(forward) / alignment)).normalize();
        double signedAngle = Math.atan2(noRollUp.cross(screenUp).dot(forward), noRollUp.dot(screenUp));
        double rollWeight = Math.min(Math.abs(alignment) / GAZE_ROLL_FADE_ALIGNMENT, 1.0);
        return (float) (-Math.toDegrees(signedAngle) * rollWeight);
    }

    /**
     * 背包等界面预览使用的无状态变换：直接取实体当前朝向与锚点，不读写世界跟随状态。
     */
    public static CatBurgerFollowTransform previewTransform(LivingEntity livingEntity, float partialTicks) {
        return composeTransform(
                livingEntity,
                partialTicks,
                livingEntity.getViewYRot(partialTicks),
                0,
                0,
                0,
                getTargetAnchorHeight(livingEntity)
        );
    }

    private static CatBurgerFollowTransform composeTransform(
            LivingEntity livingEntity,
            float partialTicks,
            float orbitYaw,
            double dragOffsetX,
            double dragOffsetY,
            double dragOffsetZ,
            double anchorHeight
    ) {
        float entityScale = livingEntity.getScale();
        double frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.get() * entityScale;
        double leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.get() * entityScale;
        double yawRadians = Math.toRadians(-orbitYaw);

        double xOffset = Math.cos(yawRadians - Mth.HALF_PI) * frontBackOffset
                - Math.cos(yawRadians) * leftRightOffset
                + dragOffsetX;

        double yOffset = anchorHeight
                + (getFloatOffset(livingEntity, partialTicks) + CatBurgerClientConfig.VERTICAL_OFFSET.get()) * entityScale
                + dragOffsetY;

        double zOffset = -Math.sin(yawRadians - Mth.HALF_PI) * frontBackOffset
                + Math.sin(yawRadians) * leftRightOffset
                + dragOffsetZ;

        return new CatBurgerFollowTransform(
                xOffset,
                yOffset,
                zOffset,
                livingEntity.getViewYRot(partialTicks),
                livingEntity.getViewXRot(partialTicks),
                0.0F,
                CatBurgerClientConfig.SCALE.get().floatValue() * entityScale
        );
    }

    /**
     * 锚点高度取实体眼高，眼高随姿态与缩放变化，潜行时再叠加 crouchOffset。
     */
    private static double getTargetAnchorHeight(LivingEntity livingEntity) {
        double eyeHeight = livingEntity.getEyeHeight();
        if (!livingEntity.isCrouching()) return eyeHeight;
        return eyeHeight + CatBurgerClientConfig.CROUCH_OFFSET.get() * livingEntity.getScale();
    }

    /**
     * 有界滞后距离：低速段斜率取位置追随的稳态滞后比 (1 - followSpeed) / followSpeed，高速段平滑饱和到 maxLagDistance。
     */
    private static double getLagDistance(double followSpeed, double motionLength) {
        double maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.get();
        if (maxLagDistance <= 0) return 0;
        double lagSpeedRatio = (1 - followSpeed) / followSpeed;
        return maxLagDistance * Math.tanh(lagSpeedRatio * motionLength / maxLagDistance);
    }

    private static double getFloatOffset(LivingEntity livingEntity, float partialTicks) {
        return CatBurgerClientConfig.FLOAT_DISTANCE.get() / 2
                * Math.sin((livingEntity.tickCount + partialTicks) * Mth.TWO_PI / CatBurgerClientConfig.FLOAT_CYCLE_DURATION.get());
    }

    /**
     * 猫猫汉堡一帧的渲染变换：相对实体脚部的偏移、由偏航俯仰与绕前向轴滚转组成的朝向，以及缩放。
     */
    public record CatBurgerFollowTransform(double xOffset, double yOffset, double zOffset, float renderYaw, float headPitch, float roll, float scale) {
        public CatBurgerFollowTransform withOrientation(float renderYaw, float headPitch, float roll) {
            return new CatBurgerFollowTransform(xOffset, yOffset, zOffset, renderYaw, headPitch, roll, scale);
        }
    }

    /**
     * 注视目标：注视点的世界坐标，以及用于抵消相机侧滚转的相机 up 与前向。
     */
    private record GazeTarget(Vec3 position, Vec3 cameraUp, Vec3 cameraForward) {
    }

    /**
     * 目标朝向：偏航、俯仰与绕前向轴的滚转。
     */
    private record Orientation(float yaw, float pitch, float roll) {
    }

    /**
     * 解析摄像机实体的注视目标，取准心命中点，摄像机实体通常就是本地玩家自己；关闭注视、非摄像机实体或无命中结果时返回 null，由调用方改用视线方向。
     */
    @Nullable
    private static GazeTarget resolveGazeTarget(LivingEntity livingEntity) {
        if (!CatBurgerClientConfig.GAZE_AT_VIEW_TARGET.get()) return null;

        Minecraft minecraft = Minecraft.getInstance();
        if (livingEntity != minecraft.getCameraEntity()) return null;

        HitResult hitResult = minecraft.hitResult;
        if (hitResult == null) return null;

        Camera camera = minecraft.gameRenderer.getMainCamera();
        return new GazeTarget(
                hitResult.getLocation(),
                new Vec3(camera.getUpVector().x, camera.getUpVector().y, camera.getUpVector().z),
                new Vec3(camera.getLookVector().x, camera.getLookVector().y, camera.getLookVector().z)
        );
    }
}
