package net.zhaiji.catburger.network.server;

import net.minecraft.server.level.ServerPlayer;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.server.packet.ClientboundFollowerRenderDataPacket;
import net.zhaiji.catburger.util.FollowerUtil;

public class ServerPacketHandler {
    /**
     * 在值进入服务端的那一刻先收束边界再写入能力实例，随后广播给追踪者并同步刷新有主生物身上的副本。
     */
    public static void handlerSyncFollowerRenderDataPacket(ServerPlayer serverPlayer, FollowerRenderData followerRenderData) {
        followerRenderData.clampToBounds();
        FollowerRenderData currentData = FollowerUtil.followerRenderDataOf(serverPlayer);
        if (currentData == null) return;
        currentData.deserializeNBT(followerRenderData.serializeNBT());
        PacketManager.sendToTrackingAndSelf(new ClientboundFollowerRenderDataPacket(serverPlayer.getUUID(), currentData), serverPlayer);
        FollowerUtil.refreshInheritedRenderData(serverPlayer, currentData);
    }
}
