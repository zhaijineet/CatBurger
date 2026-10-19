package net.zhaiji.catburger.client.render;

import net.minecraft.world.phys.Vec3;

/**
 * 朝向解算内核：模型竖轴跟随佩戴者头部，只在头部水平面内转一个局部偏航去对准注视目标。
 * <p>
 * 渲染按 Y(180 - 头部偏航) → X(-头部俯仰) → Y(-局部偏航) 施加：前两步把模型摆成与头部同一姿态，
 * 第三步绕头部竖轴转动，不改变竖轴，因此从佩戴者视角看模型恒竖直、眼线恒水平。
 * 目标高出或低于头部水平面的部分不参与对准，只保留左右会聚。
 */
public class FollowerOrientation {
    private static final double DEGENERATE_HORIZONTAL = 1.0E-4;

    /**
     * 把世界方向转进头部参考系，转后头部正前方为 +Z、头部竖轴为 +Y。
     */
    public static Vec3 toHeadFrame(Vec3 worldDirection, float headYaw, float headPitch) {
        return worldDirection.yRot((float) Math.toRadians(headYaw)).xRot((float) Math.toRadians(headPitch));
    }

    /**
     * 头部参考系方向在头部水平面内的局部偏航，与实体偏航同一正负约定。
     */
    public static float localYawOf(Vec3 headFrameDirection) {
        return (float) Math.toDegrees(Math.atan2(-headFrameDirection.x, headFrameDirection.z));
    }

    /**
     * 方向几乎沿头部竖轴时头部水平面内的方位无定义，局部偏航应保持当前值。
     */
    public static boolean isAlongHeadAxis(Vec3 headFrameDirection) {
        return Math.sqrt(headFrameDirection.x * headFrameDirection.x + headFrameDirection.z * headFrameDirection.z) < DEGENERATE_HORIZONTAL;
    }
}
