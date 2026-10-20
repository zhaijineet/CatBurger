package net.zhaiji.catburger.network.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zhaiji.catburger.network.client.ClientPacketHandler;

import java.util.function.Supplier;

/**
 * 服务端把配置卡操作结果回执给操作的玩家，客户端据此显示动作栏提示。
 */
public record ConfigCardFeedbackPacket(Component message) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeComponent(message);
    }

    public static ConfigCardFeedbackPacket decode(FriendlyByteBuf buf) {
        return new ConfigCardFeedbackPacket(buf.readComponent());
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlerConfigCardFeedbackPacket(this.message());
            });
        });
        context.setPacketHandled(true);
    }
}
