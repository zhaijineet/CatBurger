package net.zhaiji.catburger.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.config.CatBurgerCommonConfig;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CatBurgerItem extends Item implements Equipable {
    public static final String KEY_PREFIX = "item." + CatBurger.MOD_ID + ".cat_burger.";
    public static final String TOOLTIP_PREFIX = "item." + CatBurger.MOD_ID + ".tooltip.";

    private static final String NON_BREAKING_SPACE = "\u00A0";
    private static final String LINE_PREFIX = NON_BREAKING_SPACE + "•" + NON_BREAKING_SPACE;

    public CatBurgerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(KEY_PREFIX + "description.0"));
        boolean shiftDown = tooltipFlag.hasShiftDown();
        tooltipComponents.add(Component.translatable(
                TOOLTIP_PREFIX + "shift_hint",
                Component.translatable(TOOLTIP_PREFIX + "hint.shift")
                        .withStyle(shiftDown ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY)
        ).withStyle(ChatFormatting.GRAY));
        if (shiftDown) {
            appendEffectLines(tooltipComponents);
        }
    }

    private static void appendEffectLines(List<Component> tooltipComponents) {
        int foodLevelRestorationInterval = CatBurgerCommonConfig.FOOD_LEVEL_RESTORATION_INTERVAL.get();
        if (foodLevelRestorationInterval > 0) {
            tooltipComponents.add(effectLine(Component.translatable(
                    KEY_PREFIX + "effect.food_level_restoration",
                    formatNumber(foodLevelRestorationInterval / 20.0),
                    formatNumber(CatBurgerCommonConfig.FOOD_LEVEL_RESTORATION_AMOUNT.get()),
                    formatNumber(CatBurgerCommonConfig.FOOD_LEVEL_RESTORATION_LIMIT.get())
            )));
        } else {
            tooltipComponents.add(effectLine(Component.translatable(
                    KEY_PREFIX + "effect.food_level_restoration.every_tick",
                    formatNumber(CatBurgerCommonConfig.FOOD_LEVEL_RESTORATION_AMOUNT.get()),
                    formatNumber(CatBurgerCommonConfig.FOOD_LEVEL_RESTORATION_LIMIT.get())
            )));
        }

        if (!CatBurgerCommonConfig.DEATH_PROTECTION_ENABLED.get()) {
            tooltipComponents.add(effectLine(Component.translatable(KEY_PREFIX + "effect.death_protection.disabled")).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        Component healthComponent = CatBurgerCommonConfig.DEATH_PROTECTION_USE_HEALTH_PERCENTAGE.get()
                ? Component.translatable(
                        KEY_PREFIX + "effect.death_protection.restore_health_percentage",
                        formatNumber(CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_HEALTH_PERCENTAGE.get())
                )
                : Component.translatable(
                        KEY_PREFIX + "effect.death_protection.restore_health",
                        formatNumber(CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_HEALTH.get())
                );
        Component foodLevelComponent = Component.translatable(
                KEY_PREFIX + "effect.death_protection.restore_food_level",
                formatNumber(CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_FOOD_LEVEL.get()),
                formatNumber(CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_SATURATION.get())
        );
        tooltipComponents.add(effectLine(Component.empty().append(healthComponent).append(foodLevelComponent)));

        tooltipComponents.add(effectLine(Component.translatable(
                KEY_PREFIX + "effect.death_protection.cooldown",
                formatNumber(CatBurgerCommonConfig.DEATH_PROTECTION_COOLDOWN.get() / 20.0)
        )));

        if (CatBurgerCommonConfig.WAKE_UP_RESETS_DEATH_PROTECTION_COOLDOWN.get()) {
            tooltipComponents.add(effectLine(Component.translatable(KEY_PREFIX + "effect.death_protection.wake_up")));
        }
    }

    private static MutableComponent effectLine(Component content) {
        return Component.literal(LINE_PREFIX).append(content).withStyle(ChatFormatting.GRAY);
    }

    private static String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(value);
    }
}
