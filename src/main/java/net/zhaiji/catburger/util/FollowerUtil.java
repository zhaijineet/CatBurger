package net.zhaiji.catburger.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.compat.CompatManager;
import net.zhaiji.catburger.compat.curios.CuriosCompat;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.init.CatBurgerCapabilities;
import net.zhaiji.catburger.item.FollowerItem;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.server.packet.ClientboundFollowerRenderDataPacket;

import java.util.*;

/**
 * 跟随者中枢：给出原版头盔槽的身份、判定谁算跟随者、找出它戴在哪、把行为派发给它们，
 * 并把主人的渲染参数复制给有主生物。
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
     * 未佩戴任何跟随者时返回共享空列表。
     */
    public static List<WornFollower> findAll(LivingEntity wearer) {
        ItemStack headStack = wearer.getItemBySlot(EquipmentSlot.HEAD);
        boolean wearsHeadFollower = isFollower(headStack);
        List<WornFollower> curiosFollowers = CompatManager.CURIOS_LOADED ? CuriosCompat.findAllInCurios(wearer) : List.of();
        if (!wearsHeadFollower && curiosFollowers.isEmpty()) return List.of();

        List<WornFollower> wornFollowers = new ArrayList<>(curiosFollowers);

        if (wearsHeadFollower) wornFollowers.add(0, new WornFollower(headStack, EQUIPMENT_HEAD_SLOT, true));

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
     * 取实体身上的渲染参数实例，实体未挂载承载（既非玩家也非有主生物）时返回null。
     * 实例常驻，是否可用由 {@link FollowerRenderData#isSynced()} 判断。
     */
    public static FollowerRenderData followerRenderDataOf(LivingEntity livingEntity) {
        return livingEntity.getCapability(CatBurgerCapabilities.FOLLOWER_RENDER).orElse(null);
    }

    /**
     * 把主人的渲染参数复制到有主生物身上，客户端据此在本机渲染时不依赖主人是否在场。
     * 主人UUID被清空时丢弃副本，让读取方回落到本机配置。
     */
    public static void inheritOwnerRenderData(LivingEntity livingEntity) {
        if (!(livingEntity instanceof OwnableEntity ownableEntity)) return;

        UUID ownerUUID = ownableEntity.getOwnerUUID();
        if (ownerUUID == null) {
            FollowerRenderData currentData = followerRenderDataOf(livingEntity);
            if (currentData != null) currentData.markUnsynced();
            return;
        }

        ServerPlayer owner = onlineOwnerOf(livingEntity, ownerUUID);
        if (owner == null) return;

        FollowerRenderData ownerData = followerRenderDataOf(owner);
        if (ownerData == null || !ownerData.isSynced()) return;

        applyInheritedRenderData(livingEntity, ownerData);
    }

    /**
     * 从全局玩家列表里找主人，原版getOwner在服务端只遍历本维度玩家，跨维度时会漏。
     */
    private static ServerPlayer onlineOwnerOf(LivingEntity livingEntity, UUID ownerUUID) {
        if (!(livingEntity.level() instanceof ServerLevel serverLevel)) return null;

        MinecraftServer server = serverLevel.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(ownerUUID);
    }

    /**
     * 主人改完配置后刷新其名下全部有主生物的副本，横跨所有维度，否则其他维度里的生物会一直沿用旧参数。
     */
    public static void refreshInheritedRenderData(ServerPlayer owner, FollowerRenderData ownerData) {
        MinecraftServer server = owner.getServer();
        if (server == null) return;

        UUID ownerUUID = owner.getUUID();
        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (!(entity instanceof LivingEntity livingEntity)) continue;
                if (!(entity instanceof OwnableEntity ownableEntity)) continue;
                if (!ownerUUID.equals(ownableEntity.getOwnerUUID())) continue;

                applyInheritedRenderData(livingEntity, ownerData);
            }
        }
    }

    /**
     * 宠物配置同步关闭时整条链路跳过；配置卡手动设置过的实体保持自身设置，其余实体与现有副本一致就不写，避免把没有变化的数据重新广播给全部追踪者。
     */
    private static void applyInheritedRenderData(LivingEntity livingEntity, FollowerRenderData ownerData) {
        if (!CatBurgerServerConfig.PET_CONFIG_SYNC_ENABLED.get()) return;

        FollowerRenderData targetData = followerRenderDataOf(livingEntity);
        if (targetData == null) return;
        if (targetData.isManualOverride()) return;
        if (targetData.isSynced() && targetData.matches(ownerData)) return;

        targetData.deserializeNBT(ownerData.serializeNBT());

        PacketManager.sendToTrackingAndSelf(new ClientboundFollowerRenderDataPacket(livingEntity.getUUID(), true, targetData), livingEntity);
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
