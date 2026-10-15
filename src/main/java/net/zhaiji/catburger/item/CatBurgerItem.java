package net.zhaiji.catburger.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.network.client.packet.PlayerDeathPacket;
import net.zhaiji.catburger.util.TooltipUtil;

import java.util.ArrayList;
import java.util.List;

public class CatBurgerItem extends FollowerItem {
    /**
     * 佩戴时按配置的间隔恢复饥饿值，仅玩家有饥饿值，间隔为 0 时每刻恢复。
     */
    @Override
    public void tick(LivingEntity wearer, ItemStack wornStack) {
        if (!(wearer instanceof Player player)) return;

        int interval = CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_INTERVAL.get();
        if (interval > 0 && wearer.tickCount % interval != 0) return;

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        int limit = CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_LIMIT.get();
        if (foodLevel < limit) foodData.setFoodLevel(Math.min(foodLevel + CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_AMOUNT.get(), limit));
    }

    /**
     * 按配置提供死亡保护：恢复生命值、饥饿值与饱和度，写入本物品的冷却并通知佩戴者，返回 true 表示已接管本次死亡。
     */
    @Override
    public boolean onDeathProtection(LivingEntity wearer, ItemStack wornStack, ItemCooldowns cooldowns) {
        if (!CatBurgerServerConfig.DEATH_PROTECTION_ENABLED.get()) return false;
        if (cooldowns.isOnCooldown(this)) return false;

        if (CatBurgerServerConfig.USE_HEALTH_PERCENTAGE.get()) {
            float maxHealth = wearer.getMaxHealth();
            float restoreAmount = maxHealth * (CatBurgerServerConfig.RESTORE_HEALTH_PERCENTAGE.get().floatValue() / 100.0f);
            wearer.setHealth(restoreAmount);
        } else {
            wearer.setHealth(CatBurgerServerConfig.RESTORE_HEALTH.get());
        }

        Player player = wearer instanceof Player entity ? entity : null;

        if (player != null) {
            FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(CatBurgerServerConfig.RESTORE_FOOD_LEVEL.get());
            foodData.setSaturation(CatBurgerServerConfig.RESTORE_SATURATION.get());
        }

        cooldowns.addCooldown(this, CatBurgerServerConfig.COOLDOWN_DURATION.get());
        wearer.level().broadcastEntityEvent(wearer, (byte) 35);

        if (player != null) PacketDistributor.sendToPlayer((ServerPlayer) player, new PlayerDeathPacket(wornStack));

        return true;
    }

    @Override
    protected List<Component> buildEffectLines() {
        String keyPrefix = TooltipUtil.itemKeyPrefix(this);
        List<Component> effectLines = new ArrayList<>();

        int interval = CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_INTERVAL.get();
        if (interval > 0) {
            effectLines.add(TooltipUtil.effectLine(Component.translatable(
                    keyPrefix + "effect.food_level_restoration",
                    TooltipUtil.formatNumber(interval / 20.0),
                    TooltipUtil.formatNumber(CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_AMOUNT.get()),
                    TooltipUtil.formatNumber(CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_LIMIT.get())
            )));
        } else {
            effectLines.add(TooltipUtil.effectLine(Component.translatable(
                    keyPrefix + "effect.food_level_restoration.every_tick",
                    TooltipUtil.formatNumber(CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_AMOUNT.get()),
                    TooltipUtil.formatNumber(CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_LIMIT.get())
            )));
        }

        if (!CatBurgerServerConfig.DEATH_PROTECTION_ENABLED.get()) {
            effectLines.add(TooltipUtil.effectLine(Component.translatable(keyPrefix + "effect.death_protection.disabled")).withStyle(ChatFormatting.DARK_GRAY));
            return effectLines;
        }

        Component healthComponent = CatBurgerServerConfig.USE_HEALTH_PERCENTAGE.get()
                ? Component.translatable(
                keyPrefix + "effect.death_protection.restore_health_percentage",
                TooltipUtil.formatNumber(CatBurgerServerConfig.RESTORE_HEALTH_PERCENTAGE.get())
        )
                : Component.translatable(
                keyPrefix + "effect.death_protection.restore_health",
                TooltipUtil.formatNumber(CatBurgerServerConfig.RESTORE_HEALTH.get())
        );
        Component foodLevelComponent = Component.translatable(
                keyPrefix + "effect.death_protection.restore_food_level",
                TooltipUtil.formatNumber(CatBurgerServerConfig.RESTORE_FOOD_LEVEL.get()),
                TooltipUtil.formatNumber(CatBurgerServerConfig.RESTORE_SATURATION.get())
        );
        effectLines.add(TooltipUtil.effectLine(Component.empty().append(healthComponent).append(foodLevelComponent)));

        effectLines.add(TooltipUtil.effectLine(Component.translatable(
                keyPrefix + "effect.death_protection.cooldown",
                TooltipUtil.formatNumber(CatBurgerServerConfig.COOLDOWN_DURATION.get() / 20.0)
        )));

        if (CatBurgerServerConfig.RESET_COOLDOWN_ON_WAKE_UP.get())
            effectLines.add(TooltipUtil.effectLine(Component.translatable(keyPrefix + "effect.death_protection.wake_up")));

        return effectLines;
    }
}
