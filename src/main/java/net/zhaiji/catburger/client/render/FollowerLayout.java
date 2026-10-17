package net.zhaiji.catburger.client.render;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.attachment.FollowerRenderData;
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
     * 第 0 件落在配置位置，其后每件以已放置的某一件为母点随机散开；间距是绝对格数，与显示尺寸无关。
     * 与已放置件过近或落入佩戴者圆柱时重试，重试用尽仍在圆柱内则沿水平方向推出。
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
        double maxRadius = renderParams.getMaxRadius();
        float entityScale = livingEntity.getScale();
        double anchorForward = renderParams.getFrontBackOffset() * entityScale;
        double anchorRight = renderParams.getLeftRightOffset() * entityScale;
        double anchorUp = FollowerState.anchorHeightOf(livingEntity, renderParams) + renderParams.getVerticalOffset() * entityScale;
        double wearerRadius = livingEntity.getBbWidth() / 2.0 + displaySize / 2.0;

        for (int index = 1; index < followerCount; index++) {
            RandomSource random = RandomSource.create(seedOf(livingEntity, wornFollowers.get(index).slotKey()));
            double forward = 0.0;
            double right = 0.0;
            double up = 0.0;
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
                forward = layout[parent * 3] + directionForward / directionLength * distance;
                right = layout[parent * 3 + 1] + directionRight / directionLength * distance;
                up = layout[parent * 3 + 2] + directionUp / directionLength * distance;
                if (isClearOfPlaced(layout, index, forward, right, up, minSpacing)
                        && !isInsideWearer(anchorForward + forward, anchorRight + right, wearerRadius)) {
                    break;
                }
            }
            if (isInsideWearer(anchorForward + forward, anchorRight + right, wearerRadius)) {
                double horizontalForward = anchorForward + forward;
                double horizontalRight = anchorRight + right;
                double horizontalDistance = Math.sqrt(horizontalForward * horizontalForward + horizontalRight * horizontalRight);
                // 恰好落在佩戴者中轴上时没有可用的水平方向，改向正后方推出
                if (horizontalDistance < MIN_PUSH_DISTANCE) {
                    horizontalForward = -1.0;
                    horizontalRight = 0.0;
                    horizontalDistance = 1.0;
                }
                forward = horizontalForward / horizontalDistance * wearerRadius - anchorForward;
                right = horizontalRight / horizontalDistance * wearerRadius - anchorRight;
            }
            double absoluteForward = anchorForward + forward;
            double absoluteRight = anchorRight + right;
            double absoluteHorizontalDistance = Math.sqrt(absoluteForward * absoluteForward + absoluteRight * absoluteRight);
            // 圆柱比活动半径更硬：半径小于圆柱时以圆柱为准
            if (absoluteHorizontalDistance > maxRadius && maxRadius >= wearerRadius) {
                double radiusScale = maxRadius / absoluteHorizontalDistance;
                forward = absoluteForward * radiusScale - anchorForward;
                right = absoluteRight * radiusScale - anchorRight;
            }
            layout[index * 3] = forward;
            layout[index * 3 + 1] = right;
            layout[index * 3 + 2] = up;
        }

        return layout;
    }

    /**
     * 编队模式在背包预览里的无状态排布：首件在锚点，其后各件沿环绕前向的反方向按间距排成一列。
     */
    public static double[] line(int followerCount, double spacing) {
        double[] layout = new double[followerCount * 3];
        for (int index = 1; index < followerCount; index++) {
            layout[index * 3] = -spacing * index;
        }
        return layout;
    }

    private static boolean isClearOfPlaced(
            double[] layout,
            int placedCount,
            double forward,
            double right,
            double up,
            double minSpacing
    ) {
        double minSpacingSqr = minSpacing * minSpacing;
        for (int index = 0; index < placedCount; index++) {
            double deltaForward = forward - layout[index * 3];
            double deltaRight = right - layout[index * 3 + 1];
            double deltaUp = up - layout[index * 3 + 2];
            if (deltaForward * deltaForward + deltaRight * deltaRight + deltaUp * deltaUp < minSpacingSqr) {
                return false;
            }
        }

        return true;
    }

    /**
     * 佩戴者碰撞箱按以实体中轴为轴的圆柱近似，并按跟随者显示尺寸外扩，偏移相对实体脚部原点。
     */
    /**
     * 佩戴者按以实体中轴为轴的无限高圆柱处理：跟随者无论高低，水平方向都不得落入圆柱内，
     * 否则垂直移动时会被身体穿过。
     */
    private static boolean isInsideWearer(
            double forward,
            double right,
            double wearerRadius
    ) {
        return forward * forward + right * right < wearerRadius * wearerRadius;
    }
}
