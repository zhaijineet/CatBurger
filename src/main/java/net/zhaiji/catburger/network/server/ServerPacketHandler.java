package net.zhaiji.catburger.network.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.config.FollowerConfigMode;
import net.zhaiji.catburger.init.CatBurgerAttachmentTypes;
import net.zhaiji.catburger.init.CatBurgerDataComponents;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.network.client.packet.ConfigCardFeedbackPacket;
import net.zhaiji.catburger.util.FollowerConfigCardUtil;
import net.zhaiji.catburger.util.FollowerUtil;

public class ServerPacketHandler {
    /**
     * 在值进入服务端的那一刻先收束边界再写入附件，由附件机制自动广播给追踪者；
     * 是否把配置同步给宠物由服务端开关决定，关闭时玩家配置照常上传，只是不复制给有主生物。
     */
    public static void handlerSyncFollowerRenderDataPacket(ServerPlayer serverPlayer, FollowerRenderData followerRenderData) {
        followerRenderData.clampToBounds();
        serverPlayer.setData(CatBurgerAttachmentTypes.FOLLOWER_RENDER.get(), followerRenderData);
        // 叶子方法已统一判断同步开关，这里的提前返回只省掉一次附近生物遍历
        if (CatBurgerServerConfig.PET_CONFIG_SYNC_ENABLED.get()) {
            FollowerUtil.refreshInheritedRenderData(serverPlayer, followerRenderData);
        }
    }

    /**
     * 服务端自己确认玩家手持的是配置卡并推进模式，客户端只上报滚动方向；滚轮只从主手触发，副手不参与。
     */
    public static void handlerSetConfigCardModePacket(ServerPlayer serverPlayer, boolean wheelUp) {
        ItemStack mainHandItem = serverPlayer.getMainHandItem();
        if (!mainHandItem.is(CatBurgerItems.FOLLOWER_CONFIG_CARD.get())) return;

        FollowerConfigMode[] modes = FollowerConfigMode.values();
        FollowerConfigMode currentMode = FollowerConfigMode.getMode(mainHandItem);
        int offset = wheelUp ? -1 : 1;
        FollowerConfigMode newMode = modes[Math.floorMod(currentMode.ordinal() + offset, modes.length)];

        mainHandItem.set(CatBurgerDataComponents.FOLLOWER_CONFIG_MODE.get(), newMode);
        PacketDistributor.sendToPlayer(serverPlayer, new ConfigCardFeedbackPacket(FollowerConfigCardUtil.modeMessage(newMode)));
    }
}
