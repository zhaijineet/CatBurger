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
 * 把服务端收束后的渲染参数连同所属实体发给追踪者，客户端据此写入该实体的能力实例，供渲染时读取。
 * synced为false表示服务端已经撤掉这份参数，客户端收到后要把自己的实例重置回未同步，重新回落本机配置。
 */
public record ClientboundFollowerRenderDataPacket(UUID entityId, boolean synced, FollowerRenderData followerRenderData) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeBoolean(synced);
        followerRenderData.encode(buf);
    }

    public static ClientboundFollowerRenderDataPacket decode(FriendlyByteBuf buf) {
        return new ClientboundFollowerRenderDataPacket(buf.readUUID(), buf.readBoolean(), FollowerRenderData.decode(buf));
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlerClientboundFollowerRenderDataPacket(this.entityId(), this.synced(), this.followerRenderData());
            });
        });
        context.setPacketHandled(true);
    }
}
