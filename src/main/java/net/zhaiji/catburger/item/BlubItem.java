package net.zhaiji.catburger.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.config.ConfiguredEffect;
import net.zhaiji.catburger.network.client.packet.PlayerDeathPacket;
import net.zhaiji.catburger.util.TooltipUtil;

import java.util.ArrayList;
import java.util.List;

public class BlubItem extends FollowerItem {
    /**
     * 佩戴时按配置的间隔给玩家上效果，间隔明显短于效果时长，效果总在到期前被刷新，因此不会闪烁。
     */
    @Override
    public void tick(LivingEntity wearer, ItemStack wornStack) {
        if (!(wearer instanceof Player player)) return;

        int interval = CatBurgerServerConfig.BLUB_NIGHT_VISION_INTERVAL.get();
        if (interval > 0 && wearer.tickCount % interval != 0) return;

        ConfiguredEffect.resolve(CatBurgerServerConfig.BLUB_TICK_EFFECTS.get())
                        .forEach(configuredEffect -> configuredEffect.applyTo(player));
    }

    /**
     * 按配置提供死亡保护：回满生命值、削弱范围内生物、写入本物品的冷却并通知佩戴者。
     */
    @Override
    public boolean onDeathProtection(LivingEntity wearer, ItemStack wornStack, ItemCooldowns cooldowns) {
        if (!CatBurgerServerConfig.BLUB_DEATH_PROTECTION_ENABLED.get()) return false;
        if (cooldowns.isOnCooldown(this)) return false;

        if (CatBurgerServerConfig.BLUB_USE_HEALTH_PERCENTAGE.get()) {
            float maxHealth = wearer.getMaxHealth();
            wearer.setHealth(maxHealth * (CatBurgerServerConfig.BLUB_RESTORE_HEALTH_PERCENTAGE.get().floatValue() / 100.0f));
        } else {
            wearer.setHealth(CatBurgerServerConfig.BLUB_RESTORE_HEALTH.get());
        }

        List<ConfiguredEffect> radiusEffects = ConfiguredEffect.resolve(CatBurgerServerConfig.BLUB_RADIUS_EFFECTS.get());
        if (!radiusEffects.isEmpty()) {
            for (LivingEntity radiusTarget : findRadiusTargets(wearer)) {
                radiusEffects.forEach(radiusEffect -> radiusEffect.applyTo(radiusTarget));
            }
        }

        cooldowns.addCooldown(this, CatBurgerServerConfig.COOLDOWN_DURATION.get());
        wearer.level().broadcastEntityEvent(wearer, (byte) 35);

        if (wearer instanceof Player player) PacketDistributor.sendToPlayer((ServerPlayer) player, new PlayerDeathPacket(wornStack));

        return true;
    }

    /**
     * 找出范围效果的目标：以佩戴者为中心按配置半径取生物，是否命中由四个类别开关决定。
     */
    private List<LivingEntity> findRadiusTargets(LivingEntity wearer) {
        double radius = CatBurgerServerConfig.BLUB_RADIUS.get();
        List<LivingEntity> radiusTargets = new ArrayList<>();

        for (LivingEntity candidate : wearer.level().getEntitiesOfClass(LivingEntity.class, wearer.getBoundingBox().inflate(radius))) {
            if (!isAffectedCategory(wearer, candidate)) continue;
            if (candidate.distanceToSqr(wearer) > radius * radius) continue;
            radiusTargets.add(candidate);
        }

        return radiusTargets;
    }

    /**
     * 按佩戴者本人、玩家、宠物、其他生物的顺序判定候选生物属于哪个类别，并取该类别的开关。
     */
    private boolean isAffectedCategory(LivingEntity wearer, LivingEntity candidate) {
        if (candidate == wearer) return CatBurgerServerConfig.BLUB_RADIUS_AFFECT_SELF.get();
        if (candidate instanceof Player) return CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PLAYERS.get();
        if (candidate instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerUUID() != null)
            return CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PETS.get();
        return CatBurgerServerConfig.BLUB_RADIUS_AFFECT_OTHER_MOBS.get();
    }

    @Override
    protected List<Component> buildEffectLines() {
        String keyPrefix = TooltipUtil.itemKeyPrefix(this);
        List<Component> effectLines = new ArrayList<>();

        int interval = CatBurgerServerConfig.BLUB_NIGHT_VISION_INTERVAL.get();
        boolean refreshesEveryTick = interval <= 0;
        for (ConfiguredEffect tickEffect : ConfiguredEffect.resolve(CatBurgerServerConfig.BLUB_TICK_EFFECTS.get())) {
            Component tickEffectComponent = refreshesEveryTick
                    ? Component.translatable(
                    keyPrefix + "effect.tick_effects.entry.every_tick",
                    tickEffect.displayName(),
                    TooltipUtil.formatNumber(tickEffect.duration() / 20.0)
            )
                    : Component.translatable(
                    keyPrefix + "effect.tick_effects.entry",
                    TooltipUtil.formatNumber(interval / 20.0),
                    tickEffect.displayName(),
                    TooltipUtil.formatNumber(tickEffect.duration() / 20.0)
            );
            effectLines.add(TooltipUtil.effectLine(tickEffectComponent));
        }

        if (!CatBurgerServerConfig.BLUB_DEATH_PROTECTION_ENABLED.get()) return effectLines;

        effectLines.add(TooltipUtil.effectLine(buildDeathProtectionLine(keyPrefix)));
        effectLines.add(TooltipUtil.effectLine(TooltipUtil.deathProtectionCooldownComponent(keyPrefix)));
        TooltipUtil.appendDeathProtectionWakeUpLine(effectLines, keyPrefix);

        return effectLines;
    }

    /**
     * 把死亡保护的全部内容拼成一行：回血子句接范围段，范围段为空时只剩回血子句。
     */
    private Component buildDeathProtectionLine(String keyPrefix) {
        Component healthComponent = TooltipUtil.deathProtectionHealthComponent(
                keyPrefix,
                CatBurgerServerConfig.BLUB_USE_HEALTH_PERCENTAGE.get(),
                CatBurgerServerConfig.BLUB_RESTORE_HEALTH_PERCENTAGE.get(),
                CatBurgerServerConfig.BLUB_RESTORE_HEALTH.get()
        );

        List<ConfiguredEffect> radiusEffects = ConfiguredEffect.resolve(CatBurgerServerConfig.BLUB_RADIUS_EFFECTS.get());

        return Component.empty().append(healthComponent).append(buildRadiusComponent(keyPrefix, radiusEffects));
    }

    /**
     * 组装范围段：兜底类别开启时用「除…外」列出被排除的类别，关闭时正向枚举受影响的类别，无可描述内容时返回空。
     */
    private Component buildRadiusComponent(String keyPrefix, List<ConfiguredEffect> radiusEffects) {
        if (radiusEffects.isEmpty()) return Component.empty();

        boolean affectSelf = CatBurgerServerConfig.BLUB_RADIUS_AFFECT_SELF.get();
        boolean affectPlayers = CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PLAYERS.get();
        boolean affectPets = CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PETS.get();
        boolean affectOtherMobs = CatBurgerServerConfig.BLUB_RADIUS_AFFECT_OTHER_MOBS.get();

        List<Component> enabledTargets = new ArrayList<>();
        List<Component> disabledTargets = new ArrayList<>();
        collectTarget(enabledTargets, disabledTargets, affectSelf, keyPrefix + "effect.target.self");
        collectTarget(enabledTargets, disabledTargets, affectPlayers, keyPrefix + "effect.target.players");
        collectTarget(enabledTargets, disabledTargets, affectPets, keyPrefix + "effect.target.pets");

        // 兜底类别关且具体类别全关即四开关全关
        if (!affectOtherMobs && enabledTargets.isEmpty()) return Component.empty();

        String radiusText = TooltipUtil.formatNumber(CatBurgerServerConfig.BLUB_RADIUS.get());
        Component effectListComponent = joinRadiusEffects(keyPrefix, radiusEffects);

        if (affectOtherMobs && disabledTargets.isEmpty())
            return Component.translatable(
                    keyPrefix + "effect.death_protection.radius_effects.scope_all",
                    radiusText,
                    effectListComponent
            );

        if (affectOtherMobs)
            return Component.translatable(
                    keyPrefix + "effect.death_protection.radius_effects.scope_excluded",
                    radiusText,
                    joinComponents(keyPrefix, disabledTargets),
                    effectListComponent
            );

        return Component.translatable(
                keyPrefix + "effect.death_protection.radius_effects.scope",
                radiusText,
                joinComponents(keyPrefix, enabledTargets),
                effectListComponent
        );
    }

    /**
     * 按类别开关把类别名放进"受影响"或"被排除"列表，供范围段挑写法时使用。
     */
    private static void collectTarget(List<Component> enabledTargets, List<Component> disabledTargets, boolean affected, String targetKey) {
        (affected ? enabledTargets : disabledTargets).add(Component.translatable(targetKey));
    }

    /**
     * 把每条范围效果转成「效果名（秒数）」条目并按分隔符连接。
     */
    private static Component joinRadiusEffects(String keyPrefix, List<ConfiguredEffect> radiusEffects) {
        List<Component> effectComponents = new ArrayList<>();
        for (ConfiguredEffect radiusEffect : radiusEffects) {
            effectComponents.add(Component.translatable(
                    keyPrefix + "effect.death_protection.radius_effects.item",
                    radiusEffect.displayName(),
                    TooltipUtil.formatNumber(radiusEffect.duration() / 20.0)
            ));
        }

        return joinComponents(keyPrefix, effectComponents);
    }

    /**
     * 用配置里的分隔符连接若干段文本。
     */
    private static MutableComponent joinComponents(String keyPrefix, List<Component> components) {
        MutableComponent joinedComponent = Component.empty();
        for (int index = 0; index < components.size(); index++) {
            if (index > 0) joinedComponent.append(Component.translatable(keyPrefix + "effect.list_separator"));
            joinedComponent.append(components.get(index));
        }

        return joinedComponent;
    }
}
