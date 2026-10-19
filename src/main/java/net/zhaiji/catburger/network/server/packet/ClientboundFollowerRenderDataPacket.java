package net.zhaiji.catburger.network.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.network.client.ClientPacketHandler;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 把服务端收束后的渲染参数连同所属玩家发给追踪者，客户端据此写入该玩家的能力实例，供渲染时读取。
 */
public record ClientboundFollowerRenderDataPacket(UUID playerId, FollowerRenderData followerRenderData) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        followerRenderData.encode(buf);
    }

    public static ClientboundFollowerRenderDataPacket decode(FriendlyByteBuf buf) {
        return new ClientboundFollowerRenderDataPacket(buf.readUUID(), FollowerRenderData.decode(buf));
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlerClientboundFollowerRenderDataPacket(this.playerId(), this.followerRenderData());
            });
        });
        context.setPacketHandled(true);
    }
}
