package net.zhaiji.catburger.network.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.network.server.ServerPacketHandler;

import java.util.function.Supplier;

/**
 * 客户端把本机渲染参数上传给服务端，由服务端收束后写入能力实例并广播给追踪者。
 */
public record SyncFollowerRenderDataPacket(FollowerRenderData data) {
    public void encode(FriendlyByteBuf buf) {
        data.encode(buf);
    }

    public static SyncFollowerRenderDataPacket decode(FriendlyByteBuf buf) {
        return new SyncFollowerRenderDataPacket(FollowerRenderData.decode(buf));
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null)
                ServerPacketHandler.handlerSyncFollowerRenderDataPacket(context.getSender(), this.data());
        });
        context.setPacketHandled(true);
    }
}
