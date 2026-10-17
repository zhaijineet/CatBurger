package net.zhaiji.catburger.client.render;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 朝向解算内核：把目标方向转进以佩戴者视线为前方的参考系，在视线竖直平面内解出俯仰与会聚偏航。
 * <p>
 * 偏航不直接对准目标方位，是为了让 up 恒在视线竖直平面。目标方向一旦自由，模型 up 只剩绕前向一个自由度；
 * 要求它在屏幕上竖直时，前向经过正左右只有 ±90° 两个离散解，切换必然翻转180°。
 * 这是球面切向量场的拓扑限制（毛球定理），换成四元数或矩阵也不会消失：Unity、Unreal 的 lookAt 内部用四元数，
 * 同样在奇点翻转。改用世界竖直作参考则不翻转，但目标偏向侧面时 up 落在目标竖直平面，会侧歪。
 * <p>
 * 因此这里把目标压进89°锥面：任何角度都不侧歪、不翻转，代价是身后目标最多转到89°，不能完全对准。
 */
public class FollowerOrientation {
    public static final double CONE_LIMIT_DEGREES = 89.0;

    private static final double CONE_LIMIT_RADIANS = Math.toRadians(CONE_LIMIT_DEGREES);
    private static final double DEGENERATE_HORIZONTAL = 1.0E-4;

    /**
     * 把世界方向绕竖直轴转进视线系，转后正前方为 +Z、右侧为 +X。
     */
    public static Vec3 toViewFrame(Vec3 worldDirection, float viewYaw) {
        return worldDirection.yRot((float) Math.toRadians(viewYaw));
    }

    /**
     * 无注视点时，佩戴者视线方向本身在视线系里的等价方向，含俯仰。
     */
    public static Vec3 viewDirectionInViewFrame(float viewPitch) {
        double pitchRadians = Math.toRadians(viewPitch);
        return new Vec3(0.0, -Math.sin(pitchRadians), Math.cos(pitchRadians));
    }

    /**
     * 把视线系方向压进锥面；目标正在背后时方位角无定义，返回 null 表示本刻不推进。
     */
    @Nullable
    public static Vec3 clampToCone(Vec3 viewFrameDirection) {
        double horizontal = Math.sqrt(viewFrameDirection.x * viewFrameDirection.x + viewFrameDirection.y * viewFrameDirection.y);
        if (horizontal < DEGENERATE_HORIZONTAL && viewFrameDirection.z <= 0.0) return null;
        if (Math.atan2(horizontal, viewFrameDirection.z) <= CONE_LIMIT_RADIANS) return viewFrameDirection;

        double sin = Math.sin(CONE_LIMIT_RADIANS);
        double cos = Math.cos(CONE_LIMIT_RADIANS);
        return new Vec3(
                viewFrameDirection.x / horizontal * sin,
                viewFrameDirection.y / horizontal * sin,
                cos
        ).normalize();
    }

    /**
     * 视线系方向对应的俯仰，正值朝下；锥面压制后恒在 ±89° 内。
     */
    public static float pitchOf(Vec3 viewFrameDirection) {
        return (float) Math.toDegrees(Math.atan2(-viewFrameDirection.y, viewFrameDirection.z));
    }

    /**
     * 视线系方向绕自身竖直轴的局部偏航，注视会聚只体现为这个分量。
     */
    public static float localYawOf(Vec3 viewFrameDirection) {
        double horizontal = Math.sqrt(viewFrameDirection.y * viewFrameDirection.y + viewFrameDirection.z * viewFrameDirection.z);
        return (float) Math.toDegrees(Math.atan2(-viewFrameDirection.x, horizontal));
    }
}
