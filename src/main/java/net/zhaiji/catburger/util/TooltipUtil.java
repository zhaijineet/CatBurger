package net.zhaiji.catburger.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.config.CatBurgerServerConfig;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 物品 tooltip 的公共构件：键前缀、Shift 提示行、效果行包装与数值格式化。
 */
public class TooltipUtil {
    public static final String TOOLTIP_PREFIX = "item." + CatBurger.MOD_ID + ".tooltip.";

    private static final String NON_BREAKING_SPACE = "\u00A0";
    private static final String LINE_PREFIX = NON_BREAKING_SPACE + "•" + NON_BREAKING_SPACE;

    private static final Map<Item, List<Component>> EFFECT_LINE_CACHE = new ConcurrentHashMap<>();

    /**
     * 按物品注册名派生效果文案的翻译键前缀，使每个物品描述自己的效果。
     */
    public static String itemKeyPrefix(Item item) {
        return "item." + CatBurger.MOD_ID + "." + BuiltInRegistries.ITEM.getKey(item).getPath() + ".";
    }

    /**
     * 取物品的效果说明行，首次访问时用 builder 构建并缓存，使持有多件同类物品时也只各构建一次。
     */
    public static <T extends Item> List<Component> cachedEffectLines(T item, Function<T, List<Component>> builder) {
        return EFFECT_LINE_CACHE.computeIfAbsent(item, ignored -> builder.apply(item));
    }

    /**
     * 配置重载时清空效果说明行缓存，使所有物品下一帧按新配置重建。
     */
    public static void invalidateEffectLineCache() {
        EFFECT_LINE_CACHE.clear();
    }

    /**
     * 追加按住 Shift 查看详细说明的提示行，返回当前是否已展开详情。
     */
    public static boolean appendShiftHint(List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        boolean shiftDown = Screen.hasShiftDown();
        tooltipComponents.add(Component.translatable(
                TOOLTIP_PREFIX + "shift_hint",
                Component.translatable(TOOLTIP_PREFIX + "hint.shift")
                         .withStyle(shiftDown ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY)
        ).withStyle(ChatFormatting.GRAY));
        return shiftDown;
    }

    /**
     * 把一段内容包装成统一样式的效果说明行。
     */
    public static MutableComponent effectLine(Component content) {
        return Component.literal(LINE_PREFIX).append(content).withStyle(ChatFormatting.GRAY);
    }

    /**
     * 给出死亡保护的回血子句，按百分比模式或固定值模式二选一。
     */
    public static Component deathProtectionHealthComponent(String keyPrefix, boolean useHealthPercentage, double healthPercentage, double fixedHealth) {
        return useHealthPercentage
                ? Component.translatable(
                keyPrefix + "effect.death_protection.restore_health_percentage",
                formatNumber(healthPercentage)
        )
                : Component.translatable(
                keyPrefix + "effect.death_protection.restore_health",
                formatNumber(fixedHealth)
        );
    }

    /**
     * 给出死亡保护的冷却说明内容，冷却时长由所有追随者共用。
     */
    public static Component deathProtectionCooldownComponent(String keyPrefix) {
        return Component.translatable(
                keyPrefix + "effect.death_protection.cooldown",
                formatNumber(CatBurgerServerConfig.COOLDOWN_DURATION.get() / 20.0)
        );
    }

    /**
     * 按全局配置追加睡醒重置冷却的说明行，未启用时不追加。
     */
    public static void appendDeathProtectionWakeUpLine(List<Component> effectLines, String keyPrefix) {
        if (!CatBurgerServerConfig.RESET_COOLDOWN_ON_WAKE_UP.get()) return;
        effectLines.add(effectLine(Component.translatable(keyPrefix + "effect.death_protection.wake_up")));
    }

    /**
     * 按最多三位小数格式化效果数值。
     */
    public static String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(value);
    }
}
