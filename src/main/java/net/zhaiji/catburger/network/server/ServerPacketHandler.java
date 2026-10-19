package net.zhaiji.catburger.network.server;

import net.minecraft.server.level.ServerPlayer;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.init.CatBurgerAttachmentTypes;
import net.zhaiji.catburger.util.FollowerUtil;

public class ServerPacketHandler {
    /**
     * 在值进入服务端的那一刻先收束边界再写入附件，由附件机制自动广播给追踪者，并同步刷新有主生物身上的副本。
     */
    public static void handlerSyncFollowerRenderDataPacket(ServerPlayer serverPlayer, FollowerRenderData followerRenderData) {
        followerRenderData.clampToBounds();
        serverPlayer.setData(CatBurgerAttachmentTypes.FOLLOWER_RENDER.get(), followerRenderData);
        FollowerUtil.refreshInheritedRenderData(serverPlayer, followerRenderData);
    }
}
