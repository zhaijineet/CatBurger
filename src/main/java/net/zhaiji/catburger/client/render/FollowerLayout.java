package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.util.WornFollower;

import java.util.List;
import java.util.UUID;

/**
 * 独立模式下多件跟随者的散布：以配置位置为锚点凌乱散开，结果只由佩戴者 UUID、槽位键与配置决定。
 */
public class FollowerLayout {
    private static final int MAX_PLACEMENT_ATTEMPTS = 16;
    private static final double VERTICAL_SPREAD_RATIO = 0.5;
    private static final double MIN_PUSH_DISTANCE = 1.0E-4;
    private static final double FLOAT_PERIOD_SPREAD = 0.25;

    /**
     * 跨进程稳定的种子：String 的哈希由语言规范定义，UUID 随实体存档保存。
     */
    public static long seedOf(LivingEntity livingEntity, String slotKey) {
        UUID uuid = livingEntity.getUUID();
        return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits() ^ slotKey.hashCode();
    }

    /**
     * 该件跟随者的浮动相位，铺满 [0, 2π)，使多件上下浮动彼此错开。
     */
    public static double floatPhaseOf(LivingEntity livingEntity, String slotKey) {
        return RandomSource.create(seedOf(livingEntity, slotKey)).nextDouble() * Mth.TWO_PI;
    }

    /**
     * 该件跟随者的浮动周期倍率，落在 [1 - 范围, 1 + 范围] 内，与相位取自同一种子的后续随机数。
     */
    public static double floatPeriodScaleOf(LivingEntity livingEntity, String slotKey) {
        RandomSource random = RandomSource.create(seedOf(livingEntity, slotKey));
        random.nextDouble();
        return 1.0 + (random.nextDouble() * 2.0 - 1.0) * FLOAT_PERIOD_SPREAD;
    }

    /**
     * 第0件落在配置位置，其后每件以已放置的某一件为母点随机散开；间距是绝对格数，与显示尺寸无关。
     * 候选先压回圆柱与活动半径之间再判间距，全部尝试都过近时取与已放置件最远的那一次。
     */
    public static double[] scatter(
            LivingEntity livingEntity,
            List<WornFollower> wornFollowers,
            FollowerRenderData renderParams,
            double displaySize
    ) {
        int followerCount = wornFollowers.size();
        double[] layout = new double[followerCount * 3];
        double minSpacing = Math.min(renderParams.getMinSpacing(), renderParams.getMaxSpacing());
        double maxSpacing = Math.max(renderParams.getMinSpacing(), renderParams.getMaxSpacing());
        double minSpacingSqr = minSpacing * minSpacing;
        double maxRadius = renderParams.getMaxRadius();
        float entityScale = livingEntity.getScale();
        double anchorForward = renderParams.getFrontBackOffset() * entityScale;
        double anchorRight = renderParams.getLeftRightOffset() * entityScale;
        double wearerRadius = livingEntity.getBbWidth() / 2.0 + displaySize / 2.0;
        double[] constrained = new double[2];

        for (int index = 1; index < followerCount; index++) {
            RandomSource random = RandomSource.create(seedOf(livingEntity, wornFollowers.get(index).slotKey()));
            double forward = 0.0;
            double right = 0.0;
            double up = 0.0;
            double bestClearanceSqr = -1.0;
            for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
                int parent = random.nextInt(index);
                double azimuth = random.nextDouble() * Mth.TWO_PI;
                double vertical = random.nextDouble() * 2.0 - 1.0;
                double horizontal = Math.sqrt(1.0 - vertical * vertical);
                double directionForward = Math.cos(azimuth) * horizontal;
                double directionRight = Math.sin(azimuth) * horizontal;
                double directionUp = vertical * VERTICAL_SPREAD_RATIO;
                double directionLength = Math.sqrt(
                        directionForward * directionForward
                                + directionRight * directionRight
                                + directionUp * directionUp
                );
                double distance = minSpacing + random.nextDouble() * (maxSpacing - minSpacing);
                double candidateForward = layout[parent * 3] + directionForward / directionLength * distance;
                double candidateRight = layout[parent * 3 + 1] + directionRight / directionLength * distance;
                double candidateUp = layout[parent * 3 + 2] + directionUp / directionLength * distance;
                constrainToRing(
                        constrained,
                        anchorForward,
                        anchorRight,
                        candidateForward,
                        candidateRight,
                        wearerRadius,
                        maxRadius
                );
                double clearanceSqr = nearestPlacedDistanceSqr(layout, index, constrained[0], constrained[1], candidateUp);
                if (clearanceSqr >= minSpacingSqr) {
                    forward = constrained[0];
                    right = constrained[1];
                    up = candidateUp;
                    break;
                }
                if (clearanceSqr > bestClearanceSqr) {
                    bestClearanceSqr = clearanceSqr;
                    forward = constrained[0];
                    right = constrained[1];
                    up = candidateUp;
                }
            }
            layout[index * 3] = forward;
            layout[index * 3 + 1] = right;
            layout[index * 3 + 2] = up;
        }

        return layout;
    }

    /**
     * 与已放置件的最小三维距离平方：够远时用于接受候选，都不够远时用于挑出最远的那一次作兜底。
     */
    private static double nearestPlacedDistanceSqr(
            double[] layout,
            int placedCount,
            double forward,
            double right,
            double up
    ) {
        double nearestSqr = Double.MAX_VALUE;
        for (int index = 0; index < placedCount; index++) {
            double deltaForward = forward - layout[index * 3];
            double deltaRight = right - layout[index * 3 + 1];
            double deltaUp = up - layout[index * 3 + 2];
            double distanceSqr = deltaForward * deltaForward + deltaRight * deltaRight + deltaUp * deltaUp;
            if (distanceSqr < nearestSqr) {
                nearestSqr = distanceSqr;
            }
        }

        return nearestSqr;
    }

    /**
     * 把候选的相对水平位置压回允许范围：落入佩戴者圆柱时沿径向推到圆柱面上，超出活动半径时拉回半径之内。
     * 结果按相对锚点的偏移写入output数组，第0项为前向、第1项为右向。
     */
    private static void constrainToRing(
            double[] output,
            double anchorForward,
            double anchorRight,
            double candidateForward,
            double candidateRight,
            double wearerRadius,
            double maxRadius
    ) {
        double absoluteForward = anchorForward + candidateForward;
        double absoluteRight = anchorRight + candidateRight;
        double horizontalDistance = Math.sqrt(absoluteForward * absoluteForward + absoluteRight * absoluteRight);
        // 佩戴者按以实体中轴为轴的无限高圆柱处理：无论高低，水平落入圆柱就会在垂直移动时被身体穿过
        if (horizontalDistance < wearerRadius) {
            // 恰好落在佩戴者中轴上时没有可用的水平方向，改向正后方推出
            if (horizontalDistance < MIN_PUSH_DISTANCE) {
                absoluteForward = -1.0;
                absoluteRight = 0.0;
                horizontalDistance = 1.0;
            }
            double pushScale = wearerRadius / horizontalDistance;
            absoluteForward *= pushScale;
            absoluteRight *= pushScale;
            horizontalDistance = wearerRadius;
        }
        // 圆柱比活动半径更硬：半径小于圆柱时以圆柱为准
        if (horizontalDistance > maxRadius && maxRadius >= wearerRadius) {
            double radiusScale = maxRadius / horizontalDistance;
            absoluteForward *= radiusScale;
            absoluteRight *= radiusScale;
        }
        output[0] = absoluteForward - anchorForward;
        output[1] = absoluteRight - anchorRight;
    }
}
