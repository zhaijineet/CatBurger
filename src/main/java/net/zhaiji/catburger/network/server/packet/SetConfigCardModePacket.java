package net.zhaiji.catburger.network.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhaiji.catburger.network.server.ServerPacketHandler;

import java.util.function.Supplier;

/**
 * 客户端滚轮上报滚动方向，模式由服务端按当前值推进后再回执，避免客户端预测与服务端显示不一致。
 */
public record SetConfigCardModePacket(boolean wheelUp) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(wheelUp);
    }

    public static SetConfigCardModePacket decode(FriendlyByteBuf buf) {
        return new SetConfigCardModePacket(buf.readBoolean());
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null)
                ServerPacketHandler.handlerSetConfigCardModePacket(context.getSender(), this.wheelUp());
        });
        context.setPacketHandled(true);
    }
}
