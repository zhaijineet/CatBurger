package net.zhaiji.catburger.network.server.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.network.server.ServerPacketHandler;

/**
 * 客户端滚轮上报滚动方向，模式由服务端按当前值推进后再回执，避免客户端预测与服务端显示不一致。
 */
public record SetConfigCardModePacket(boolean wheelUp) implements CustomPacketPayload {
    public static final Type<SetConfigCardModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "set_config_card_mode_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetConfigCardModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SetConfigCardModePacket::wheelUp,
            SetConfigCardModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(SetConfigCardModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
                ServerPacketHandler.handlerSetConfigCardModePacket(serverPlayer, packet.wheelUp());
        });
    }
}
