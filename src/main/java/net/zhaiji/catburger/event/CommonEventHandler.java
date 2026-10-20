package net.zhaiji.catburger.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.capability.FollowerRenderDataProvider;
import net.zhaiji.catburger.init.CatBurgerCapabilities;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.server.packet.ClientboundFollowerRenderDataPacket;
import net.zhaiji.catburger.util.FollowerInteractUtil;
import net.zhaiji.catburger.util.FollowerUtil;

public class CommonEventHandler {
    public static void handlerLivingDeathEvent(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (FollowerUtil.onDeathProtection(player, player.getCooldowns())) event.setCanceled(true);
    }

    /**
     * 拦截不带命中位置的实体交互，名单内的物品手持右键生物时取消原版交互，让物品自己的右键逻辑生效。
     */
    public static void handlerPlayerInteractEvent$EntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (FollowerInteractUtil.shouldCancelEntityInteract(event.getItemStack(), event.getTarget())) event.setCanceled(true);
    }

    /**
     * 拦截带命中位置的精确实体交互，与不带位置的版本共用同一套名单判据，区别只是事件额外携带了命中位置。
     */
    public static void handlerPlayerInteractEvent$EntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (FollowerInteractUtil.shouldCancelEntityInteract(event.getItemStack(), event.getTarget())) event.setCanceled(true);
    }

    /**
     * 追踪开始时补发一次渲染参数：有主生物先按主人刷新副本，玩家直接取服务端能力实例，只发给新追踪者。
     */
    public static void handlerPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        FollowerUtil.inheritOwnerRenderData(livingEntity);

        FollowerRenderData renderData = FollowerUtil.followerRenderDataOf(livingEntity);
        if (renderData == null || !renderData.isSynced()) return;

        PacketManager.sendToClient(new ClientboundFollowerRenderDataPacket(livingEntity.getUUID(), true, renderData), serverPlayer);
    }

    /**
     * 玩家重生会新建实体，能力实例随之重建，这里把旧实体的渲染参数复制过去并重新发给追踪者，对齐1.21.1附件的copyOnDeath。
     */
    public static void handlerPlayerEvent$Clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        FollowerRenderData originalData = FollowerUtil.followerRenderDataOf(event.getOriginal());
        FollowerRenderData clonedData = FollowerUtil.followerRenderDataOf(event.getEntity());
        if (originalData == null || !originalData.isSynced() || clonedData == null) return;

        clonedData.deserializeNBT(originalData.serializeNBT());
        PacketManager.sendToTrackingAndSelf(new ClientboundFollowerRenderDataPacket(event.getEntity().getUUID(), true, clonedData), event.getEntity());
    }

    public static void handlerPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        FollowerUtil.onWakeUp(player, player.getCooldowns());
    }

    public static void handlerLivingEvent$LivingTickEvent(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide()) return;
        FollowerUtil.tick(livingEntity);
    }

    /**
     * 给所有生物挂上渲染参数的能力承载，配置卡可以把参数写到任意生物身上，覆盖面必须与1.21.1的附件一致；
     * 客户端同样要挂，否则追踪者收到同步数据后无处存放。
     */
    public static void handlerAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof LivingEntity)) return;
        if (event.getCapabilities().containsKey(CatBurgerCapabilities.ID)) return;

        // 挂到所有生物身上是为了让配置卡能作用于任意生物；未同步实例不写内容，
        // 但 Forge 的 CapabilityDispatcher 仍会为每只生物写一条空的 ForgeCaps 条目，这是该挂载面的既定代价
        event.addCapability(CatBurgerCapabilities.ID, new FollowerRenderDataProvider());
    }
}
