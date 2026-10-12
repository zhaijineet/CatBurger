package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zhaiji.catburger.config.CatBurgerClientConfig;

import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public final class CatBurgerFollowState {
    private static final Map<LivingEntity, CatBurgerFollowState> STATES = new WeakHashMap<>();

    private static final double MIN_MOTION_LENGTH = 1.0E-4;
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

    private CatBurgerFollowState(LivingEntity livingEntity) {
        lastTick = livingEntity.tickCount;
        lastEntityX = livingEntity.getX();
        lastEntityY = livingEntity.getY();
        lastEntityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        lastHeadYaw = targetYaw = dragYaw = prevDragYaw = springYaw = prevSpringYaw = headYaw;
        anchorHeight = prevAnchorHeight = getTargetAnchorHeight(livingEntity);
    }

    public static CatBurgerFollowState of(LivingEntity livingEntity) {
        return STATES.computeIfAbsent(livingEntity, CatBurgerFollowState::new);
    }

    public void update(LivingEntity livingEntity) {
        int currentTick = livingEntity.tickCount;
        if (currentTick == lastTick) {
            return;
        }

        double entityX = livingEntity.getX();
        double entityY = livingEntity.getY();
        double entityZ = livingEntity.getZ();
        float headYaw = livingEntity.getYHeadRot();
        double targetAnchorHeight = getTargetAnchorHeight(livingEntity);

        if (currentTick - lastTick > TICK_GAP_RESET_THRESHOLD) {
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

        if (CatBurgerClientConfig.DRAG_ENABLED.get()) {
            double dragStrength = CatBurgerClientConfig.DRAG_STRENGTH.get();
            double motionX = entityX - lastEntityX;
            double motionY = entityY - lastEntityY;
            double motionZ = entityZ - lastEntityZ;
            double motionLength = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            double targetOffsetX = 0;
            double targetOffsetY = 0;
            double targetOffsetZ = 0;
            if (motionLength > MIN_MOTION_LENGTH) {
                double lagDistance = getLagDistance(dragStrength, motionLength);
                targetOffsetX = -motionX / motionLength * lagDistance;
                targetOffsetY = -motionY / motionLength * lagDistance;
                targetOffsetZ = -motionZ / motionLength * lagDistance;
            }

            offsetX += (targetOffsetX - offsetX) * dragStrength;
            offsetY += (targetOffsetY - offsetY) * dragStrength;
            offsetZ += (targetOffsetZ - offsetZ) * dragStrength;
            anchorHeight += (targetAnchorHeight - anchorHeight) * dragStrength;
        } else {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
            anchorHeight = targetAnchorHeight;
        }

        if (CatBurgerClientConfig.ROTATION_DRAG_ENABLED.get()) {
            float angleDiff = CatBurgerClientConfig.ROTATION_DRAG_USE_WRAP_DEGREES.get()
                    ? Mth.wrapDegrees(targetYaw - dragYaw)
                    : targetYaw - dragYaw;
            dragYaw += angleDiff * CatBurgerClientConfig.ROTATION_DRAG_SMOOTHNESS.get().floatValue();
        } else {
            dragYaw = targetYaw;
        }

        if (CatBurgerClientConfig.SPRING_ENABLED.get()) {
            float displacement = targetYaw - springYaw;
            float springForce = CatBurgerClientConfig.SPRING_STIFFNESS.get().floatValue() * displacement;
            springVelocity = springVelocity * CatBurgerClientConfig.SPRING_DAMPING.get().floatValue() + springForce;
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
    }

    public CatBurgerFollowTransform resolve(LivingEntity livingEntity, float partialTicks) {
        float usedYaw;
        if (CatBurgerClientConfig.SPRING_ENABLED.get()) {
            usedYaw = Mth.lerp(partialTicks, prevSpringYaw, springYaw);
        } else if (CatBurgerClientConfig.ROTATION_DRAG_ENABLED.get()) {
            usedYaw = Mth.lerp(partialTicks, prevDragYaw, dragYaw);
        } else {
            usedYaw = livingEntity.getViewYRot(partialTicks);
        }

        return composeTransform(
                livingEntity,
                partialTicks,
                usedYaw,
                Mth.lerp(partialTicks, prevOffsetX, offsetX),
                Mth.lerp(partialTicks, prevOffsetY, offsetY),
                Mth.lerp(partialTicks, prevOffsetZ, offsetZ),
                Mth.lerp(partialTicks, prevAnchorHeight, anchorHeight)
        );
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
            float usedYaw,
            double dragOffsetX,
            double dragOffsetY,
            double dragOffsetZ,
            double anchorHeight
    ) {
        float entityScale = livingEntity.getScale();
        double frontBackOffset = CatBurgerClientConfig.FRONT_BACK_OFFSET.get() * entityScale;
        double leftRightOffset = CatBurgerClientConfig.LEFT_RIGHT_OFFSET.get() * entityScale;
        double yawRadians = Math.toRadians(-usedYaw);

        double xOffset = Math.cos(yawRadians - Mth.HALF_PI) * frontBackOffset
                - Math.cos(yawRadians) * leftRightOffset
                + dragOffsetX;

        double yOffset = anchorHeight
                + (getFloatOffset(livingEntity, partialTicks) + CatBurgerClientConfig.VERTICAL_OFFSET.get()) * entityScale
                + dragOffsetY;

        double zOffset = -Math.sin(yawRadians - Mth.HALF_PI) * frontBackOffset
                + Math.sin(yawRadians) * leftRightOffset
                + dragOffsetZ;

        float renderYaw;
        if (
                CatBurgerClientConfig.SPRING_ENABLED.get()
                || CatBurgerClientConfig.ROTATION_DRAG_ENABLED.get() && CatBurgerClientConfig.ROTATION_DRAG_AFFECT_ORIENTATION.get()
        ) {
            renderYaw = usedYaw;
        } else {
            renderYaw = livingEntity.getViewYRot(partialTicks);
        }

        return new CatBurgerFollowTransform(
                xOffset,
                yOffset,
                zOffset,
                renderYaw,
                livingEntity.getViewXRot(partialTicks),
                CatBurgerClientConfig.SCALE.get().floatValue() * entityScale
        );
    }

    /**
     * 锚点高度取实体眼高（随姿态与缩放变化），潜行时再叠加 crouchOffset。
     */
    private static double getTargetAnchorHeight(LivingEntity livingEntity) {
        double eyeHeight = livingEntity.getEyeHeight();
        if (!livingEntity.isCrouching()) {
            return eyeHeight;
        }
        return eyeHeight + CatBurgerClientConfig.CROUCH_OFFSET.get() * livingEntity.getScale();
    }

    /**
     * 有界滞后距离：低速段斜率取位置追随的稳态滞后比 (1 - dragStrength) / dragStrength，高速段平滑饱和到 maxLagDistance。
     */
    private static double getLagDistance(double dragStrength, double motionLength) {
        double maxLagDistance = CatBurgerClientConfig.MAX_LAG_DISTANCE.get();
        if (maxLagDistance <= 0) {
            return 0;
        }
        double lagSpeedRatio = (1 - dragStrength) / dragStrength;
        return maxLagDistance * Math.tanh(lagSpeedRatio * motionLength / maxLagDistance);
    }

    private static double getFloatOffset(LivingEntity livingEntity, float partialTicks) {
        return CatBurgerClientConfig.FLOAT_DISTANCE.get() / 2
                * Math.sin((livingEntity.tickCount + partialTicks) * Mth.TWO_PI / CatBurgerClientConfig.FLOAT_CYCLE_DURATION.get());
    }

    public record CatBurgerFollowTransform(double xOffset, double yOffset, double zOffset, float renderYaw, float headPitch, float scale) {
    }
}
