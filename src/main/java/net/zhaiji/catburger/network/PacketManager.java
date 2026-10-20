package net.zhaiji.catburger.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zhaiji.catburger.network.client.packet.ConfigCardFeedbackPacket;
import net.zhaiji.catburger.network.client.packet.PlayerDeathPacket;
import net.zhaiji.catburger.network.server.packet.SetConfigCardModePacket;
import net.zhaiji.catburger.network.server.packet.SyncFollowerRenderDataPacket;

public class PacketManager {
    public static final String VERSION = "1.0";

    public static void handlerRegisterPayloadHandlersEvent(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                PlayerDeathPacket.TYPE,
                PlayerDeathPacket.STREAM_CODEC,
                PlayerDeathPacket::handler
        );

        registrar.playToClient(
                ConfigCardFeedbackPacket.TYPE,
                ConfigCardFeedbackPacket.STREAM_CODEC,
                ConfigCardFeedbackPacket::handler
        );

        registrar.playToServer(
                SyncFollowerRenderDataPacket.TYPE,
                SyncFollowerRenderDataPacket.STREAM_CODEC,
                SyncFollowerRenderDataPacket::handler
        );

        registrar.playToServer(
                SetConfigCardModePacket.TYPE,
                SetConfigCardModePacket.STREAM_CODEC,
                SetConfigCardModePacket::handler
        );
    }
}
