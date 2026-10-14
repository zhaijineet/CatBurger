package net.zhaiji.catburger.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.compat.CompatManager;
import net.zhaiji.catburger.compat.curios.CuriosCompat;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.*;

/**
 * 跟随者中枢：给出原版头盔槽的身份、判定谁算跟随者、找出它戴在哪、把行为派发给它们。
 */
public class FollowerUtil {
    /**
     * 原版头盔槽的槽位键。原版装备槽与 Curios 槽是两个独立容器，各自占据一个键空间。
     */
    public static final String EQUIPMENT_HEAD_SLOT = "equipment:head";

    /**
     * 判定物品是否是跟随者，即物品继承了 {@link FollowerItem}。
     */
    public static boolean isFollower(ItemStack itemStack) {
        return itemStack.getItem() instanceof FollowerItem;
    }

    /**
     * 找出该实体当前佩戴的全部跟随者，顺序固定为原版头盔槽优先、Curios 槽其次（顺序由 compat 层给出）。
     * 未佩戴任何跟随者时返回共享空列表；有 Curios 槽位的实体每 tick 至多扫描一次槽位，由 Curios 的同刻缓存复用。
     */
    public static List<WornFollower> findAll(LivingEntity wearer) {
        ItemStack headStack = wearer.getItemBySlot(EquipmentSlot.HEAD);
        boolean wearsHeadFollower = isFollower(headStack);
        List<WornFollower> curiosFollowers = CompatManager.CURIOS_LOADED ? CuriosCompat.findAllInCurios(wearer) : List.of();
        if (!wearsHeadFollower && curiosFollowers.isEmpty()) return List.of();

        List<WornFollower> wornFollowers = new ArrayList<>(curiosFollowers);

        if (wearsHeadFollower) wornFollowers.addFirst(new WornFollower(headStack, EQUIPMENT_HEAD_SLOT, true));

        return wornFollowers;
    }

    /**
     * 佩戴者每 tick 触发一次，触发间隔由实现方负责。
     */
    public static void tick(LivingEntity wearer) {
        resolveAll(wearer).forEach((followerItem, wornStack) -> followerItem.tick(wearer, wornStack));
    }

    /**
     * 死亡保护入口，返回 true 表示已有实现方接管本次死亡，调用方据此取消死亡事件。
     */
    public static boolean onDeathProtection(LivingEntity wearer, ItemCooldowns cooldowns) {
        for (Map.Entry<FollowerItem, ItemStack> entry : resolveAll(wearer).entrySet()) {
            if (entry.getKey().onDeathProtection(wearer, entry.getValue(), cooldowns)) return true;
        }

        return false;
    }

    /**
     * 佩戴者睡醒时触发，是否重置冷却由实现方决定。
     */
    public static void onWakeUp(LivingEntity wearer, ItemCooldowns cooldowns) {
        resolveAll(wearer).forEach((followerItem, wornStack) -> followerItem.onWakeUp(wearer, wornStack, cooldowns));
    }

    /**
     * 把佩戴的跟随者按物品去重，同一物品戴在多处只派发优先级最高的那件，未佩戴时返回共享空映射。
     */
    private static Map<FollowerItem, ItemStack> resolveAll(LivingEntity wearer) {
        List<WornFollower> wornFollowers = findAll(wearer);
        if (wornFollowers.isEmpty()) return Map.of();

        Map<FollowerItem, ItemStack> resolvedFollowers = new LinkedHashMap<>();
        for (WornFollower wornFollower : wornFollowers) {
            if (wornFollower.itemStack().getItem() instanceof FollowerItem followerItem)
                resolvedFollowers.putIfAbsent(followerItem, wornFollower.itemStack());
        }

        return resolvedFollowers;
    }
}
