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
import org.jetbrains.annotations.Nullable;

/**
 * 观察者侧的注视解算：给出跟随者该朝向的注视点，是本类渲染逻辑里唯一读取本机准心的地方。
 */
public class GazeResolver {
    /**
     * 解析注视点的世界坐标：摄像机实体直接取本机准心命中点，其他玩家按他自己的眼位、朝向与交互距离在本地重算，
     * 非玩家实体没有准心概念故返回 null 由调用方改用视线方向。
     */
    @Nullable
    public static Vec3 gazePositionOf(LivingEntity livingEntity, FollowerRenderData renderParams) {
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
