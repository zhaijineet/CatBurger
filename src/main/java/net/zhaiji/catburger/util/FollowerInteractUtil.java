package net.zhaiji.catburger.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 配置卡与生物交互的辅助：登记需要拦下原版交互的物品，并负责按视线选出作用目标。
 */
public class FollowerInteractUtil {
    private static final List<Supplier<Item>> BLOCKED_ITEMS = new ArrayList<>();

    public static void register(Supplier<Item> item) {
        BLOCKED_ITEMS.add(item);
    }

    /**
     * 只看本次交互使用的那只手，避免卡放在副手时误伤主手的原版交互。
     */
    public static boolean shouldCancelEntityInteract(ItemStack usedItem, Entity target) {
        if (!(target instanceof LivingEntity)) return false;

        for (Supplier<Item> blockedItem : BLOCKED_ITEMS) {
            if (usedItem.is(blockedItem.get())) return true;
        }
        return false;
    }

    /**
     * 取玩家视线方向上最近的生物，距离在原版实体交互范围上放宽一格，与服务端交互校验的容差一致。
     */
    public static LivingEntity findTarget(Player player) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                player,
                entity -> entity != player && entity instanceof LivingEntity,
                player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 1.0
        );
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}
