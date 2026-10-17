package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 编队模式的尾巴：首件之后的各节以世界坐标记录，每节追随前一节并归位到它正后方一个间距处，
 * 整条尾巴最终沿首件环绕方向的反向排成一列；各节朝向复用阶段2 的视线系内核。
 */
public class FollowerTail {
    private static final double MIN_SEGMENT_DISTANCE = 1.0E-4;
    private static final double DEGENERATE_GAZE_DISTANCE = 0.1;

    private double[] positions = new double[0];
    private double[] previousPositions = new double[0];
    private float[] yaws = new float[0];
    private float[] previousYaws = new float[0];
    private float[] pitches = new float[0];
    private float[] previousPitches = new float[0];
    private float[] gazeYaws = new float[0];
    private float[] previousGazeYaws = new float[0];
    private int segmentCount;

    /**
     * 调整节数：新增的节依次接在前一节（第一节接在首件）的正后方并取佩戴者视线朝向，多出的节直接丢弃。
     */
    public void resize(int segmentCount, Vec3 leaderPosition, Vec3 backward, float viewYaw, float viewPitch, double spacing) {
        if (segmentCount == this.segmentCount) return;

        int keptCount = Math.min(segmentCount, this.segmentCount);
        positions = Arrays.copyOf(positions, segmentCount * 3);
        previousPositions = Arrays.copyOf(previousPositions, segmentCount * 3);
        yaws = Arrays.copyOf(yaws, segmentCount);
        previousYaws = Arrays.copyOf(previousYaws, segmentCount);
        pitches = Arrays.copyOf(pitches, segmentCount);
        previousPitches = Arrays.copyOf(previousPitches, segmentCount);
        gazeYaws = Arrays.copyOf(gazeYaws, segmentCount);
        previousGazeYaws = Arrays.copyOf(previousGazeYaws, segmentCount);
        for (int segment = keptCount; segment < segmentCount; segment++) {
            int base = segment * 3;
            double aheadX = segment == 0 ? leaderPosition.x : positions[base - 3];
            double aheadY = segment == 0 ? leaderPosition.y : positions[base - 2];
            double aheadZ = segment == 0 ? leaderPosition.z : positions[base - 1];
            positions[base] = previousPositions[base] = aheadX + backward.x * spacing;
            positions[base + 1] = previousPositions[base + 1] = aheadY + backward.y * spacing;
            positions[base + 2] = previousPositions[base + 2] = aheadZ + backward.z * spacing;
            snapOrientation(segment, viewYaw, viewPitch);
        }

        this.segmentCount = segmentCount;
    }

    /**
     * 把整条尾巴重排到首件正后方的一条直线上并取佩戴者视线朝向，同时拉平上一刻，不从旧位置插值过来。
     */
    public void reset(Vec3 leaderPosition, Vec3 backward, float viewYaw, float viewPitch, double spacing) {
        for (int segment = 0; segment < segmentCount; segment++) {
            int base = segment * 3;
            double distance = spacing * (segment + 1);
            positions[base] = previousPositions[base] = leaderPosition.x + backward.x * distance;
            positions[base + 1] = previousPositions[base + 1] = leaderPosition.y + backward.y * distance;
            positions[base + 2] = previousPositions[base + 2] = leaderPosition.z + backward.z * distance;
            snapOrientation(segment, viewYaw, viewPitch);
        }
    }

    /**
     * 第一节与首件的距离超过上限时视为脱节，通常由佩戴者瞬移造成。
     */
    public boolean isDetached(Vec3 leaderPosition, double maxDistance) {
        if (segmentCount == 0) return false;

        double deltaX = positions[0] - leaderPosition.x;
        double deltaY = positions[1] - leaderPosition.y;
        double deltaZ = positions[2] - leaderPosition.z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > maxDistance * maxDistance;
    }

    /**
     * 推进一刻：每节以已推进的前一节为准，按跟随速度趋近它正后方一个间距的归位点，落入佩戴者圆柱时沿水平方向推出，
     * 随后各节按自己的位置注视佩戴者的注视点。
     */
    public void advance(
            Vec3 leaderPosition,
            Vec3 backward,
            double spacing,
            double followSpeed,
            @Nullable Vec3 gazePosition,
            boolean gazeFrontFollower,
            float viewYaw,
            float viewPitch,
            float orientationFollowSpeed,
            LivingEntity wearer,
            double displaySize
    ) {
        System.arraycopy(positions, 0, previousPositions, 0, segmentCount * 3);
        System.arraycopy(yaws, 0, previousYaws, 0, segmentCount);
        System.arraycopy(pitches, 0, previousPitches, 0, segmentCount);
        System.arraycopy(gazeYaws, 0, previousGazeYaws, 0, segmentCount);
        double wearerRadius = wearer.getBbWidth() / 2.0 + displaySize / 2.0;
        double aheadX = leaderPosition.x;
        double aheadY = leaderPosition.y;
        double aheadZ = leaderPosition.z;
        for (int segment = 0; segment < segmentCount; segment++) {
            int base = segment * 3;
            positions[base] += (aheadX + backward.x * spacing - positions[base]) * followSpeed;
            positions[base + 1] += (aheadY + backward.y * spacing - positions[base + 1]) * followSpeed;
            positions[base + 2] += (aheadZ + backward.z * spacing - positions[base + 2]) * followSpeed;
            pushOutOfWearer(base, wearer.getX(), wearer.getZ(), wearerRadius);
            advanceOrientation(
                    segment,
                    new Vec3(aheadX, aheadY, aheadZ),
                    gazePosition,
                    gazeFrontFollower,
                    viewYaw,
                    viewPitch,
                    orientationFollowSpeed
            );
            aheadX = positions[base];
            aheadY = positions[base + 1];
            aheadZ = positions[base + 2];
        }
    }

    /**
     * 佩戴者按以实体中轴为轴的无限高圆柱处理，半径按跟随者显示尺寸外扩；
     * 跟随者无论高低，水平方向都不得落入圆柱内，否则垂直移动时会被身体穿过。
     */
    private void pushOutOfWearer(int base, double centerX, double centerZ, double radius) {
        double offsetX = positions[base] - centerX;
        double offsetZ = positions[base + 2] - centerZ;
        double horizontalDistance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        // 恰好在中轴上时没有可用的水平方向，留给下一刻的归位拉开
        if (horizontalDistance >= radius || horizontalDistance < MIN_SEGMENT_DISTANCE) return;

        positions[base] = centerX + offsetX / horizontalDistance * radius;
        positions[base + 2] = centerZ + offsetZ / horizontalDistance * radius;
    }

    /**
     * 与首件同一套内核推进该节：偏航取佩戴者视线；注视目标按配置取佩戴者注视点或前一节位置，
     * 会聚拆成锥面压制后的俯仰与局部偏航；注视点与该节几乎重合或锥面方位角无定义时保持当前朝向。
     */
    private void advanceOrientation(
            int segment,
            Vec3 aheadPosition,
            @Nullable Vec3 gazePosition,
            boolean gazeFrontFollower,
            float viewYaw,
            float viewPitch,
            float orientationFollowSpeed
    ) {
        float targetPitch = viewPitch;
        float targetGazeYaw = 0.0F;
        Vec3 lookTarget = gazeFrontFollower ? aheadPosition : gazePosition;
        if (lookTarget != null) {
            int base = segment * 3;
            Vec3 toTarget = lookTarget.subtract(positions[base], positions[base + 1], positions[base + 2]);
            if (toTarget.lengthSqr() <= DEGENERATE_GAZE_DISTANCE * DEGENERATE_GAZE_DISTANCE) return;

            Vec3 clampedDirection = FollowerOrientation.clampToCone(FollowerOrientation.toViewFrame(toTarget, viewYaw));
            if (clampedDirection == null) return;

            targetPitch = FollowerOrientation.pitchOf(clampedDirection);
            targetGazeYaw = FollowerOrientation.localYawOf(clampedDirection);
        }

        yaws[segment] += Mth.wrapDegrees(viewYaw - yaws[segment]) * orientationFollowSpeed;
        pitches[segment] += (targetPitch - pitches[segment]) * orientationFollowSpeed;
        gazeYaws[segment] += Mth.wrapDegrees(targetGazeYaw - gazeYaws[segment]) * orientationFollowSpeed;
    }

    private void snapOrientation(int segment, float viewYaw, float viewPitch) {
        yaws[segment] = previousYaws[segment] = viewYaw;
        pitches[segment] = previousPitches[segment] = viewPitch;
        gazeYaws[segment] = previousGazeYaws[segment] = 0.0F;
    }

    public double x(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousPositions[segment * 3], positions[segment * 3]);
    }

    public double y(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousPositions[segment * 3 + 1], positions[segment * 3 + 1]);
    }

    public double z(int segment, float partialTicks) {
        return Mth.lerp(partialTicks, previousPositions[segment * 3 + 2], positions[segment * 3 + 2]);
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
