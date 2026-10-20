package net.zhaiji.catburger.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.network.server.packet.SyncFollowerRenderDataPacket;
import net.zhaiji.catburger.util.FollowerConfigCardUtil;

public class ClientPacketHandler {
    public static void handlerPlayerDeathPacket(ItemStack itemStack) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(itemStack);
    }

    public static void handlerConfigCardFeedbackPacket(Component message) {
        FollowerConfigCardUtil.showMessage(Minecraft.getInstance().player, message);
    }

    /**
     * 把本机渲染参数上传给服务端，未连接服务器时跳过。
     */
    public static void sendFollowerRenderData() {
        if (Minecraft.getInstance().getConnection() == null) return;

        PacketDistributor.sendToServer(new SyncFollowerRenderDataPacket(FollowerRenderData.fromClientConfig()));
    }
}
