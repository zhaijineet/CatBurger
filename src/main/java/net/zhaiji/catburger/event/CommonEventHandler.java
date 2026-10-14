package net.zhaiji.catburger.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zhaiji.catburger.util.FollowerUtil;

public class CommonEventHandler {
    public static void handlerLivingDeathEvent(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (FollowerUtil.onDeathProtection(player, player.getCooldowns())) event.setCanceled(true);
    }

    public static void handlerPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        FollowerUtil.onWakeUp(player, player.getCooldowns());
    }

    public static void handlerEntityTickEvent$Post(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.level().isClientSide()) return;
        FollowerUtil.tick(livingEntity);
    }
}
