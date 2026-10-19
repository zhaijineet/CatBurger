package net.zhaiji.catburger.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.util.TooltipUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * 跟随者物品基类：承载只有物品才能做到的装备槽与 tooltip 能力，端行为则由子类各自实现。
 */
public abstract class FollowerItem extends Item {
    public FollowerItem(Properties properties) {
        super(properties.stacksTo(1).equippable(EquipmentSlot.HEAD));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String keyPrefix = TooltipUtil.itemKeyPrefix(itemStack.getItem());
        tooltipComponents.accept(Component.translatable(keyPrefix + "description.0"));

        List<Component> effectLines = TooltipUtil.cachedEffectLines(this, FollowerItem::buildEffectLines);
        if (effectLines.isEmpty()) return;

        if (TooltipUtil.appendShiftHint(tooltipComponents, tooltipFlag)) effectLines.forEach(tooltipComponents);
    }

    /**
     * 佩戴者每 tick 调用一次，佩戴者可以是任意生物，触发间隔与实体类型限定由实现方自己负责。
     */
    public abstract void tick(LivingEntity wearer, ItemStack wornStack);

    /**
     * 佩戴者将死时调用，返回 true 表示已接管本次死亡，调用方据此取消死亡事件。
     */
    public abstract boolean onDeathProtection(LivingEntity wearer, ItemStack wornStack, ItemCooldowns cooldowns);

    /**
     * 睡醒时默认重置本物品的死亡保护冷却，子类如需特殊行为再覆写。
     */
    public void onWakeUp(LivingEntity wearer, ItemStack wornStack, ItemCooldowns cooldowns) {
        if (!CatBurgerServerConfig.RESET_COOLDOWN_ON_WAKE_UP.get()) return;
        cooldowns.removeCooldown(cooldowns.getCooldownGroup(wornStack));
    }

    /**
     * 子类描述自身的效果说明行。
     */
    protected abstract List<Component> buildEffectLines();
}
