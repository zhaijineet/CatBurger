package net.zhaiji.catburger.network.client.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.network.client.ClientPacketHandler;

/**
 * 服务端把配置卡操作结果回执给操作的玩家，客户端据此显示动作栏提示。
 */
public record ConfigCardFeedbackPacket(Component message) implements CustomPacketPayload {
    public static final Type<ConfigCardFeedbackPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CatBurger.MOD_ID, "config_card_feedback_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigCardFeedbackPacket> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            ConfigCardFeedbackPacket::message,
            ConfigCardFeedbackPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(ConfigCardFeedbackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handlerConfigCardFeedbackPacket(packet.message()));
    }
}
