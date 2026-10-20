package net.zhaiji.catburger.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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

    public static void handlerPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.level().isClientSide()) return;
        FollowerUtil.inheritOwnerRenderData(livingEntity);
    }

    public static void handlerPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        FollowerUtil.onWakeUp(player, player.getCooldowns());
    }

    public static void handlerEntityTickEvent$Post(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.level().isClientSide()) return;
        FollowerUtil.tick(livingEntity);
    }
}
