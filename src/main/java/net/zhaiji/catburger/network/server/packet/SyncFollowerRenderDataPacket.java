package net.zhaiji.catburger.network.server.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.network.server.ServerPacketHandler;

/**
 * 客户端把本机渲染参数上传给服务端，由服务端收束后写入附件并自动广播给追踪者。
 */
public record SyncFollowerRenderDataPacket(FollowerRenderData data) implements CustomPacketPayload {
    public static final Type<SyncFollowerRenderDataPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CatBurger.MOD_ID, "sync_follower_render_data_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFollowerRenderDataPacket> STREAM_CODEC = StreamCodec.composite(
            FollowerRenderData.STREAM_CODEC,
            SyncFollowerRenderDataPacket::data,
            SyncFollowerRenderDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(SyncFollowerRenderDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
                ServerPacketHandler.handlerSyncFollowerRenderDataPacket(serverPlayer, packet.data());
        });
    }
}
