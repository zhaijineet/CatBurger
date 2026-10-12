package net.zhaiji.catburger.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zhaiji.catburger.config.CatBurgerCommonConfig;
import net.zhaiji.catburger.equipment.CatBurgerEquipment;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.network.client.packet.PlayerDeathPacket;

public class CommonEventHandler {
    public static void handlerLivingDeathEvent(LivingDeathEvent event) {
        if (!CatBurgerCommonConfig.DEATH_PROTECTION_ENABLED.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Item item = CatBurgerItems.CAT_BURGER.get();
        if (player.getCooldowns().isOnCooldown(item)) return;
        if (CatBurgerEquipment.find(player).isEmpty()) return;

        FoodData foodData = player.getFoodData();
        if (CatBurgerCommonConfig.USE_HEALTH_PERCENTAGE.get()) {
            float maxHealth = player.getMaxHealth();
            float restoreAmount = maxHealth * (CatBurgerCommonConfig.RESTORE_HEALTH_PERCENTAGE.get().floatValue() / 100.0f);
            player.setHealth(restoreAmount);
        } else {
            player.setHealth(CatBurgerCommonConfig.RESTORE_HEALTH.get());
        }
        foodData.setFoodLevel(CatBurgerCommonConfig.RESTORE_FOOD_LEVEL.get());
        foodData.setSaturation(CatBurgerCommonConfig.RESTORE_SATURATION.get());
        player.getCooldowns().addCooldown(item, CatBurgerCommonConfig.COOLDOWN.get());
        player.level().broadcastEntityEvent(player, (byte) 35);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new PlayerDeathPacket());
        event.setCanceled(true);
    }

    public static void handlerPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        if (!CatBurgerCommonConfig.RESET_COOLDOWN_ON_WAKE_UP.get()) return;
        Player player = event.getEntity();
        if (CatBurgerEquipment.find(player).isEmpty()) return;
        player.getCooldowns().removeCooldown(CatBurgerItems.CAT_BURGER.get());
    }

    public static void handlerPlayerTickEvent$Post(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        int interval = CatBurgerCommonConfig.INTERVAL.get();
        if (interval > 0 && player.tickCount % interval != 0) return;
        if (CatBurgerEquipment.find(player).isEmpty()) return;

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        int limit = CatBurgerCommonConfig.LIMIT.get();
        if (foodLevel < limit) {
            foodData.setFoodLevel(Math.min(foodLevel + CatBurgerCommonConfig.AMOUNT.get(), limit));
        }
    }
}
