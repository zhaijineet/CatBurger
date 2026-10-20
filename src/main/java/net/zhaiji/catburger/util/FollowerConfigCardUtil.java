package net.zhaiji.catburger.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.zhaiji.catburger.config.FollowerConfigMode;
import net.zhaiji.catburger.init.CatBurgerItems;

/**
 * 配置卡的动作栏提示与服务端回执，文本键统一取自配置卡自己的前缀。
 */
public class FollowerConfigCardUtil {
    /**
     * 构造“追随者配置卡：”加带色模式名的提示内容，服务端回执与客户端显示共用。
     */
    public static Component modeMessage(FollowerConfigMode mode) {
        return Component.empty()
                        .append(Component.translatable(keyPrefix() + "hud"))
                        .append(Component.translatable(keyPrefix() + "mode." + mode.getSerializedName()).withStyle(mode.getColor()));
    }

    public static Component message(String messageKey) {
        return Component.translatable(keyPrefix() + messageKey);
    }

    /**
     * 显示服务端回执的提示，只在客户端调用。
     */
    public static void showMessage(Player player, Component message) {
        player.sendOverlayMessage(message);
    }

    private static String keyPrefix() {
        return TooltipUtil.itemKeyPrefix(CatBurgerItems.FOLLOWER_CONFIG_CARD.get());
    }
}
