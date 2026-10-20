package net.zhaiji.catburger.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.network.client.packet.ConfigCardFeedbackPacket;
import net.zhaiji.catburger.network.client.packet.PlayerDeathPacket;
import net.zhaiji.catburger.network.server.packet.ClientboundFollowerRenderDataPacket;
import net.zhaiji.catburger.network.server.packet.SetConfigCardModePacket;
import net.zhaiji.catburger.network.server.packet.SyncFollowerRenderDataPacket;

public class PacketManager {
    public static final String VERSION = "1.1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    public static void registry() {
        int id = 0;
        INSTANCE.messageBuilder(PlayerDeathPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerDeathPacket::encode)
                .decoder(PlayerDeathPacket::decode)
                .consumerMainThread(PlayerDeathPacket::handler)
                .add();
        INSTANCE.messageBuilder(SyncFollowerRenderDataPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SyncFollowerRenderDataPacket::encode)
                .decoder(SyncFollowerRenderDataPacket::decode)
                .consumerMainThread(SyncFollowerRenderDataPacket::handler)
                .add();
        INSTANCE.messageBuilder(ClientboundFollowerRenderDataPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundFollowerRenderDataPacket::encode)
                .decoder(ClientboundFollowerRenderDataPacket::decode)
                .consumerMainThread(ClientboundFollowerRenderDataPacket::handler)
                .add();
        INSTANCE.messageBuilder(ConfigCardFeedbackPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ConfigCardFeedbackPacket::encode)
                .decoder(ConfigCardFeedbackPacket::decode)
                .consumerMainThread(ConfigCardFeedbackPacket::handler)
                .add();
        INSTANCE.messageBuilder(SetConfigCardModePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetConfigCardModePacket::encode)
                .decoder(SetConfigCardModePacket::decode)
                .consumerMainThread(SetConfigCardModePacket::handler)
                .add();
    }

    public static <MSG> void sendToClient(MSG msg, ServerPlayer serverPlayer) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    /**
     * 发给该实体的全部追踪者；实体是玩家时连它自己一起发，用于把服务端收束后的渲染参数同步回客户端。
     */
    public static <MSG> void sendToTrackingAndSelf(MSG msg, Entity trackedEntity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> trackedEntity), msg);
    }
}
