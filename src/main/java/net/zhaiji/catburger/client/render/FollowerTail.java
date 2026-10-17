package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 编队模式的尾巴：每节拆成形状层与漂移层两层状态。
 * 形状层是以累计环绕角、水平半径、高度记录的极坐标，只表达绕佩戴者的旋转，归位点由前一节的形状加静止队形步长得到；
 * 漂移层是世界笛卡尔偏移，只表达平移惯性，避让与圆柱修正也写回这里，因此转身与平移互不污染。
 * 各节朝向复用与首件同一个头部参考系内核，注视前一节时改用世界系 lookAt。
 */
public class FollowerTail {
    private static final double MIN_SEGMENT_DISTANCE = 1.0E-4;
    private static final double DEGENERATE_GAZE_DISTANCE = 0.1;

    private double[] angles = new double[0];
    private double[] previousAngles = new double[0];
    private double[] radii = new double[0];
    private double[] previousRadii = new double[0];
    private double[] heights = new double[0];
    private double[] previousHeights = new double[0];
    private double[] driftXs = new double[0];
    private double[] previousDriftXs = new double[0];
    private double[] driftYs = new double[0];
    private double[] previousDriftYs = new double[0];
    private double[] driftZs = new double[0];
    private double[] previousDriftZs = new double[0];
    private float[] yaws = new float[0];
    private float[] previousYaws = new float[0];
    private float[] pitches = new float[0];
    private float[] previousPitches = new float[0];
    private float[] gazeYaws = new float[0];
    private float[] previousGazeYaws = new float[0];
    private int segmentCount;

    /**
     * 环绕角与半径换算成世界偏移的横向分量，角度取与实体偏航同一约定。
     */
    private static double offsetX(double angle, double radius) {
        return -Math.sin(Math.toRadians(angle)) * radius;
    }

    /**
     * 环绕角与半径换算成世界偏移的纵向分量，角度取与实体偏航同一约定。
     */
    private static double offsetZ(double angle, double radius) {
        return Math.cos(Math.toRadians(angle)) * radius;
    }

    /**
     * 世界偏移的水平方位角，约定与环绕角一致，结果落在 -180 到 180 之间。
     */
    private static double azimuthOf(double x, double z) {
        return Math.toDegrees(Math.atan2(-x, z));
    }

    /**
     * tanh 有界饱和的缩放倍数：把长度 length 的偏差整体乘它，使长度趋近上限而处处光滑；上限取 0 时偏差清零。
     */
    private static double saturatedScale(double length, double maxDistance) {
        if (maxDistance <= 0) return 0.0;
        return maxDistance * Math.tanh(length / maxDistance) / length;
    }

    /**
     * 静止队形第 segment 节相对前一个点的极坐标步长，依次为环绕角差、水平半径差与高度差，供归位与新节展开共用；
     * 前一个点在第0节时取首件。静止队形本身穿过佩戴者中轴时相邻两点方位差接近 180、步长有歧义，属配置退化。
     */
    private static void staticFormationStep(
            int segment,
            double leaderAngle,
            double leaderRadius,
            double leaderHeight,
            Vec3 backward,
            double spacing,
            double[] step
    ) {
        double aheadOffsetX = offsetX(leaderAngle, leaderRadius) + backward.x * spacing * segment;
        double aheadOffsetY = leaderHeight + backward.y * spacing * segment;
        double aheadOffsetZ = offsetZ(leaderAngle, leaderRadius) + backward.z * spacing * segment;
        double segmentOffsetX = aheadOffsetX + backward.x * spacing;
        double segmentOffsetY = aheadOffsetY + backward.y * spacing;
        double segmentOffsetZ = aheadOffsetZ + backward.z * spacing;
        step[0] = Mth.wrapDegrees(azimuthOf(segmentOffsetX, segmentOffsetZ) - azimuthOf(aheadOffsetX, aheadOffsetZ));
        step[1] = Math.sqrt(segmentOffsetX * segmentOffsetX + segmentOffsetZ * segmentOffsetZ)
                - Math.sqrt(aheadOffsetX * aheadOffsetX + aheadOffsetZ * aheadOffsetZ);
        step[2] = segmentOffsetY - aheadOffsetY;
    }

    /**
     * 调整节数：新增的节依次接在前一节（第一节接在首件）的正后方并取佩戴者视线朝向，多出的节直接丢弃；
     * 已有节的两层状态保持不变，新增节的形状沿静止队形步长接续、漂移取前一节的漂移。
     */
    public void resize(
            int segmentCount,
            double leaderAngle,
            double leaderRadius,
            double leaderHeight,
            Vec3 leaderDrift,
            Vec3 backward,
            float viewYaw,
            float viewPitch,
            double spacing
    ) {
        if (segmentCount == this.segmentCount) return;

        int keptCount = Math.min(segmentCount, this.segmentCount);
        angles = Arrays.copyOf(angles, segmentCount);
        previousAngles = Arrays.copyOf(previousAngles, segmentCount);
        radii = Arrays.copyOf(radii, segmentCount);
        previousRadii = Arrays.copyOf(previousRadii, segmentCount);
        heights = Arrays.copyOf(heights, segmentCount);
        previousHeights = Arrays.copyOf(previousHeights, segmentCount);
        driftXs = Arrays.copyOf(driftXs, segmentCount);
        previousDriftXs = Arrays.copyOf(previousDriftXs, segmentCount);
        driftYs = Arrays.copyOf(driftYs, segmentCount);
        previousDriftYs = Arrays.copyOf(previousDriftYs, segmentCount);
        driftZs = Arrays.copyOf(driftZs, segmentCount);
        previousDriftZs = Arrays.copyOf(previousDriftZs, segmentCount);
        yaws = Arrays.copyOf(yaws, segmentCount);
        previousYaws = Arrays.copyOf(previousYaws, segmentCount);
        pitches = Arrays.copyOf(pitches, segmentCount);
        previousPitches = Arrays.copyOf(previousPitches, segmentCount);
        gazeYaws = Arrays.copyOf(gazeYaws, segmentCount);
        previousGazeYaws = Arrays.copyOf(previousGazeYaws, segmentCount);
        double referenceAngle = keptCount == 0 ? leaderAngle : angles[keptCount - 1];
        double referenceRadius = keptCount == 0 ? leaderRadius : radii[keptCount - 1];
        double referenceHeight = keptCount == 0 ? leaderHeight : heights[keptCount - 1];
        double referenceDriftX = keptCount == 0 ? leaderDrift.x : driftXs[keptCount - 1];
        double referenceDriftY = keptCount == 0 ? leaderDrift.y : driftYs[keptCount - 1];
        double referenceDriftZ = keptCount == 0 ? leaderDrift.z : driftZs[keptCount - 1];
        double[] step = new double[3];
        for (int segment = keptCount; segment < segmentCount; segment++) {
            staticFormationStep(segment, leaderAngle, leaderRadius, leaderHeight, backward, spacing, step);
            referenceAngle += step[0];
            referenceRadius += step[1];
            referenceHeight += step[2];
            angles[segment] = previousAngles[segment] = referenceAngle;
            radii[segment] = previousRadii[segment] = referenceRadius;
            heights[segment] = previousHeights[segment] = referenceHeight;
            driftXs[segment] = previousDriftXs[segment] = referenceDriftX;
            driftYs[segment] = previousDriftYs[segment] = referenceDriftY;
            driftZs[segment] = previousDriftZs[segment] = referenceDriftZ;
            snapOrientation(segment, viewYaw, viewPitch);
        }

        this.segmentCount = segmentCount;
    }

    /**
     * 把整条尾巴重排到首件正后方的一条直线上并取佩戴者视线朝向，同时拉平上一刻，不从旧位置插值过来；
     * 形状沿静止队形步长从首件的累计角逐节接续，漂移直接取首件的漂移。
     */
    public void reset(
            double leaderAngle,
            double leaderRadius,
            double leaderHeight,
            Vec3 leaderDrift,
            Vec3 backward,
            float viewYaw,
            float viewPitch,
            double spacing
    ) {
        double referenceAngle = leaderAngle;
        double referenceRadius = leaderRadius;
        double referenceHeight = leaderHeight;
        double[] step = new double[3];
        for (int segment = 0; segment < segmentCount; segment++) {
            staticFormationStep(segment, leaderAngle, leaderRadius, leaderHeight, backward, spacing, step);
            referenceAngle += step[0];
            referenceRadius += step[1];
            referenceHeight += step[2];
            angles[segment] = previousAngles[segment] = referenceAngle;
            radii[segment] = previousRadii[segment] = referenceRadius;
            heights[segment] = previousHeights[segment] = referenceHeight;
            driftXs[segment] = previousDriftXs[segment] = leaderDrift.x;
            driftYs[segment] = previousDriftYs[segment] = leaderDrift.y;
            driftZs[segment] = previousDriftZs[segment] = leaderDrift.z;
            snapOrientation(segment, viewYaw, viewPitch);
        }
    }

    /**
     * 脱节判定：首件与第0节、任意相邻两节最终偏移的三维距离超过上限，或任一节两层状态出现非有限值，
     * 即视为脱节，通常由佩戴者瞬移或坐标损坏造成；上限的语义是相邻两节的最大允许距离，
     * 正常运动下相邻两节的距离上界为静止队形间距、形状层拖尾饱和与漂移层差饱和三者之和再加一次避让推开量，故取两倍（间距加最大拖尾距离）。
     */
    public boolean isDetached(
            double leaderAngle,
            double leaderRadius,
            double leaderHeight,
            Vec3 leaderDrift,
            double maxDistance
    ) {
        if (segmentCount == 0) return false;

        double aheadOffsetX = offsetX(leaderAngle, leaderRadius) + leaderDrift.x;
        double aheadOffsetY = leaderHeight + leaderDrift.y;
        double aheadOffsetZ = offsetZ(leaderAngle, leaderRadius) + leaderDrift.z;
        double maxDistanceSquared = maxDistance * maxDistance;
        for (int segment = 0; segment < segmentCount; segment++) {
            if (!Double.isFinite(angles[segment]) || !Double.isFinite(radii[segment]) || !Double.isFinite(heights[segment])
                    || !Double.isFinite(driftXs[segment]) || !Double.isFinite(driftYs[segment]) || !Double.isFinite(driftZs[segment])) {
                return true;
            }

            double segmentOffsetX = offsetX(angles[segment], radii[segment]) + driftXs[segment];
            double segmentOffsetY = heights[segment] + driftYs[segment];
            double segmentOffsetZ = offsetZ(angles[segment], radii[segment]) + driftZs[segment];
            double deltaX = segmentOffsetX - aheadOffsetX;
            double deltaY = segmentOffsetY - aheadOffsetY;
            double deltaZ = segmentOffsetZ - aheadOffsetZ;
            // 距离出现非有限值时同样视为脱节
            if (!(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ <= maxDistanceSquared)) return true;

            aheadOffsetX = segmentOffsetX;
            aheadOffsetY = segmentOffsetY;
            aheadOffsetZ = segmentOffsetZ;
        }
        return false;
    }

    /**
     * 半径下限是硬约束：水平距离不足 wearerRadius 时按自身水平方向推到圆柱面上，水平距离过小时取该节形状角方向，
     * 高度分量保持不变。
     */
    private void applyRadiusFloor(int segment, double[] segmentOffset, double wearerRadius) {
        double horizontalDistance = Math.sqrt(segmentOffset[0] * segmentOffset[0] + segmentOffset[2] * segmentOffset[2]);
        if (horizontalDistance >= wearerRadius) return;

        if (horizontalDistance < MIN_SEGMENT_DISTANCE) {
            segmentOffset[0] = offsetX(angles[segment], wearerRadius);
            segmentOffset[2] = offsetZ(angles[segment], wearerRadius);
            return;
        }

        double floorScale = wearerRadius / horizontalDistance;
        segmentOffset[0] *= floorScale;
        segmentOffset[2] *= floorScale;
    }

    /**
     * 推进一刻：形状层按前一节的累计角加静止队形步长趋近归位点并沿圆柱饱和，只表达绕佩戴者的旋转；
     * 漂移层先扣掉佩戴者位移再跟随前一节的漂移并做同样的有界饱和，只表达平移惯性；
     * 两层相加得到最终偏移，随后按跟随速度消解与首件和已推进各节的穿插、把最终位置推出佩戴者圆柱，
     * 修正量全部写回漂移层，形状层保持纯旋转；最后各节按自己的位置转向注视目标。
     */
    public void advance(
            double leaderAngle,
            double leaderRadius,
            double leaderHeight,
            Vec3 leaderDrift,
            Vec3 backward,
            Vec3 wearerMotion,
            double spacing,
            double followSpeed,
            double maxLagDistance,
            @Nullable Vec3 gazePosition,
            boolean gazeFrontFollower,
            float headYaw,
            float headPitch,
            float orientationFollowSpeed,
            Vec3 wearerPosition,
            LivingEntity wearer,
            double displaySize
    ) {
        System.arraycopy(angles, 0, previousAngles, 0, segmentCount);
        System.arraycopy(radii, 0, previousRadii, 0, segmentCount);
        System.arraycopy(heights, 0, previousHeights, 0, segmentCount);
        System.arraycopy(driftXs, 0, previousDriftXs, 0, segmentCount);
        System.arraycopy(driftYs, 0, previousDriftYs, 0, segmentCount);
        System.arraycopy(driftZs, 0, previousDriftZs, 0, segmentCount);
        System.arraycopy(yaws, 0, previousYaws, 0, segmentCount);
        System.arraycopy(pitches, 0, previousPitches, 0, segmentCount);
        System.arraycopy(gazeYaws, 0, previousGazeYaws, 0, segmentCount);

        double wearerRadius = wearer.getBbWidth() / 2.0 + displaySize / 2.0;
        double minGap = Math.min(displaySize, spacing);
        // 第0槽存首件的最终偏移，其后每槽存已推进完的一节，供避让逐节查询
        double[] occupiedOffsets = new double[(segmentCount + 1) * 3];
        occupiedOffsets[0] = offsetX(leaderAngle, leaderRadius) + leaderDrift.x;
        occupiedOffsets[1] = leaderHeight + leaderDrift.y;
        occupiedOffsets[2] = offsetZ(leaderAngle, leaderRadius) + leaderDrift.z;
        double[] step = new double[3];
        double referenceAngle = leaderAngle;
        double referenceRadius = leaderRadius;
        double referenceHeight = leaderHeight;
        double referenceDriftX = leaderDrift.x;
        double referenceDriftY = leaderDrift.y;
        double referenceDriftZ = leaderDrift.z;
        for (int segment = 0; segment < segmentCount; segment++) {
            int aheadBase = segment * 3;
            staticFormationStep(segment, leaderAngle, leaderRadius, leaderHeight, backward, spacing, step);
            double homeAngle = referenceAngle + step[0];
            double homeRadius = referenceRadius + step[1];
            double homeHeight = referenceHeight + step[2];

            angles[segment] += (homeAngle - angles[segment]) * followSpeed;
            radii[segment] += (homeRadius - radii[segment]) * followSpeed;
            heights[segment] += (homeHeight - heights[segment]) * followSpeed;
            saturateLag(segment, homeAngle, homeRadius, homeHeight, wearerRadius, maxLagDistance);
            radii[segment] = Math.max(radii[segment], wearerRadius);

            driftXs[segment] -= wearerMotion.x;
            driftYs[segment] -= wearerMotion.y;
            driftZs[segment] -= wearerMotion.z;
            driftXs[segment] += (referenceDriftX - driftXs[segment]) * followSpeed;
            driftYs[segment] += (referenceDriftY - driftYs[segment]) * followSpeed;
            driftZs[segment] += (referenceDriftZ - driftZs[segment]) * followSpeed;
            saturateDrift(segment, referenceDriftX, referenceDriftY, referenceDriftZ, maxLagDistance);

            double shapeX = offsetX(angles[segment], radii[segment]);
            double shapeY = heights[segment];
            double shapeZ = offsetZ(angles[segment], radii[segment]);
            double[] finalOffset = {
                    shapeX + driftXs[segment],
                    shapeY + driftYs[segment],
                    shapeZ + driftZs[segment]
            };
            // 软避让：本刻只消解穿插的一部分，穿插在若干刻内消解，首件因此能从相邻节旁滑过
            for (int obstacle = 0; obstacle <= segment; obstacle++) {
                int obstacleBase = obstacle * 3;
                double gapX = finalOffset[0] - occupiedOffsets[obstacleBase];
                double gapY = finalOffset[1] - occupiedOffsets[obstacleBase + 1];
                double gapZ = finalOffset[2] - occupiedOffsets[obstacleBase + 2];
                double gapDistance = Math.sqrt(gapX * gapX + gapY * gapY + gapZ * gapZ);
                if (gapDistance >= minGap || gapDistance < MIN_SEGMENT_DISTANCE) continue;

                double pushScale = (minGap - gapDistance) * followSpeed / gapDistance;
                finalOffset[0] += gapX * pushScale;
                finalOffset[1] += gapY * pushScale;
                finalOffset[2] += gapZ * pushScale;
            }
            applyRadiusFloor(segment, finalOffset, wearerRadius);
            // 修正量写回漂移层，形状层保持纯旋转
            driftXs[segment] = finalOffset[0] - shapeX;
            driftYs[segment] = finalOffset[1] - shapeY;
            driftZs[segment] = finalOffset[2] - shapeZ;

            referenceAngle = angles[segment];
            referenceRadius = radii[segment];
            referenceHeight = heights[segment];
            referenceDriftX = driftXs[segment];
            referenceDriftY = driftYs[segment];
            referenceDriftZ = driftZs[segment];

            int occupiedBase = (segment + 1) * 3;
            occupiedOffsets[occupiedBase] = finalOffset[0];
            occupiedOffsets[occupiedBase + 1] = finalOffset[1];
            occupiedOffsets[occupiedBase + 2] = finalOffset[2];

            Vec3 segmentPosition = wearerPosition.add(finalOffset[0], finalOffset[1], finalOffset[2]);
            if (gazeFrontFollower) {
                advanceLookAtOrientation(
                        segment,
                        segmentPosition,
                        wearerPosition.add(
                                occupiedOffsets[aheadBase],
                                occupiedOffsets[aheadBase + 1],
                                occupiedOffsets[aheadBase + 2]
                        ),
                        orientationFollowSpeed
                );
            } else {
                advanceOrientation(
                        segment,
                        segmentPosition,
                        gazePosition,
                        headYaw,
                        headPitch,
                        orientationFollowSpeed
                );
            }
        }
    }

    /**
     * 形状层的拖尾饱和：本节相对归位点的偏差按弧长、半径差与高度差度量，三段统一乘 tanh 系数，
     * maxLagDistance 取 0 时直接落在归位点；弧长取的半径不小于本节实际半径与归位点半径，使该度量恒为真实位移的上界。
     */
    private void saturateLag(
            int segment,
            double homeAngle,
            double homeRadius,
            double homeHeight,
            double wearerRadius,
            double maxLagDistance
    ) {
        double arcRadius = Math.max(radii[segment], Math.max(homeRadius, wearerRadius));
        double arcOffset = Math.toRadians(angles[segment] - homeAngle) * arcRadius;
        double radiusOffset = radii[segment] - homeRadius;
        double heightOffset = heights[segment] - homeHeight;
        double offsetLength = Math.sqrt(
                arcOffset * arcOffset + radiusOffset * radiusOffset + heightOffset * heightOffset
        );
        if (offsetLength < MIN_SEGMENT_DISTANCE) return;

        double lagScale = saturatedScale(offsetLength, maxLagDistance);
        angles[segment] = homeAngle + (angles[segment] - homeAngle) * lagScale;
        radii[segment] = homeRadius + radiusOffset * lagScale;
        heights[segment] = homeHeight + heightOffset * lagScale;
    }

    /**
     * 漂移层的拖尾饱和：本节漂移相对前一节的偏差按 tanh 有界，上限 maxLagDistance 取 0 时直接贴合前一节。
     */
    private void saturateDrift(
            int segment,
            double aheadDriftX,
            double aheadDriftY,
            double aheadDriftZ,
            double maxLagDistance
    ) {
        double lagX = driftXs[segment] - aheadDriftX;
        double lagY = driftYs[segment] - aheadDriftY;
        double lagZ = driftZs[segment] - aheadDriftZ;
        double lagLength = Math.sqrt(lagX * lagX + lagY * lagY + lagZ * lagZ);
        if (lagLength < MIN_SEGMENT_DISTANCE) return;

        double lagScale = saturatedScale(lagLength, maxLagDistance);
        driftXs[segment] = aheadDriftX + lagX * lagScale;
        driftYs[segment] = aheadDriftY + lagY * lagScale;
        driftZs[segment] = aheadDriftZ + lagZ * lagScale;
    }

    /**
     * 与首件同一套内核推进该节：竖轴跟随头部朝向，无注视目标时局部偏航为 0，
     * 有目标时在头部参考系里对准它；目标与该节几乎重合或沿头部竖轴时保持当前局部偏航。
     */
    private void advanceOrientation(
            int segment,
            Vec3 segmentPosition,
            @Nullable Vec3 lookTarget,
            float headYaw,
            float headPitch,
            float orientationFollowSpeed
    ) {
        yaws[segment] += Mth.wrapDegrees(headYaw - yaws[segment]) * orientationFollowSpeed;
        pitches[segment] += (headPitch - pitches[segment]) * orientationFollowSpeed;

        float targetGazeYaw = 0.0F;
        if (lookTarget != null) {
            Vec3 toTarget = lookTarget.subtract(segmentPosition);
            if (toTarget.lengthSqr() <= DEGENERATE_GAZE_DISTANCE * DEGENERATE_GAZE_DISTANCE) return;

            Vec3 headFrameDirection = FollowerOrientation.toHeadFrame(toTarget, yaws[segment], pitches[segment]);
            if (FollowerOrientation.isAlongHeadAxis(headFrameDirection)) return;

            targetGazeYaw = FollowerOrientation.localYawOf(headFrameDirection);
        }

        gazeYaws[segment] += Mth.wrapDegrees(targetGazeYaw - gazeYaws[segment]) * orientationFollowSpeed;
    }

    /**
     * 世界系对准前一节：偏航与俯仰直接取本节到前一节的方向、局部偏航归零，竖轴不随头部俯仰倾斜；
     * 方向水平分量过小时偏航保持当前值、只更新俯仰，两者过近时整体保持。
     */
    private void advanceLookAtOrientation(
            int segment,
            Vec3 segmentPosition,
            Vec3 lookTarget,
            float orientationFollowSpeed
    ) {
        float targetYaw = yaws[segment];
        float targetPitch = pitches[segment];
        Vec3 toTarget = lookTarget.subtract(segmentPosition);
        if (toTarget.lengthSqr() > DEGENERATE_GAZE_DISTANCE * DEGENERATE_GAZE_DISTANCE) {
            double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            if (horizontalDistance >= MIN_SEGMENT_DISTANCE) {
                targetYaw = (float) azimuthOf(toTarget.x, toTarget.z);
            }
            targetPitch = (float) Math.toDegrees(
                    Math.atan2(-toTarget.y, Math.max(horizontalDistance, MIN_SEGMENT_DISTANCE))
            );
        }

        yaws[segment] += Mth.wrapDegrees(targetYaw - yaws[segment]) * orientationFollowSpeed;
        pitches[segment] += (targetPitch - pitches[segment]) * orientationFollowSpeed;
        gazeYaws[segment] += -gazeYaws[segment] * orientationFollowSpeed;
    }

    private void snapOrientation(int segment, float headYaw, float headPitch) {
        yaws[segment] = previousYaws[segment] = headYaw;
        pitches[segment] = previousPitches[segment] = headPitch;
        gazeYaws[segment] = previousGazeYaws[segment] = 0.0F;
    }

    /**
     * 取该节相对佩戴者渲染原点的横向偏移：形状层按极坐标插值换算，再叠加线性插值的漂移。
     */
    public double x(int segment, float partialTicks) {
        double angle = Mth.lerp(partialTicks, previousAngles[segment], angles[segment]);
        double radius = Mth.lerp(partialTicks, previousRadii[segment], radii[segment]);
        return offsetX(angle, radius) + Mth.lerp(partialTicks, previousDriftXs[segment], driftXs[segment]);
    }

    /**
     * 取该节相对佩戴者渲染原点的高度偏移：形状层高度与漂移各按线性插值后相加。
     */
    public double y(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousHeights[segment], heights[segment])
                + Mth.lerp(partialTicks, previousDriftYs[segment], driftYs[segment]);
    }

    /**
     * 取该节相对佩戴者渲染原点的纵向偏移：形状层按极坐标插值换算，再叠加线性插值的漂移。
     */
    public double z(int segment, float partialTicks) {
        double angle = Mth.lerp(partialTicks, previousAngles[segment], angles[segment]);
        double radius = Mth.lerp(partialTicks, previousRadii[segment], radii[segment]);
        return offsetZ(angle, radius) + Mth.lerp(partialTicks, previousDriftZs[segment], driftZs[segment]);
    }

    public float yaw(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousYaws[segment], yaws[segment]);
    }

    public float pitch(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousPitches[segment], pitches[segment]);
    }

    public float gazeYaw(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousGazeYaws[segment], gazeYaws[segment]);
    }
}
