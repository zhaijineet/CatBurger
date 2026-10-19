package net.zhaiji.catburger.network.client.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.zhaiji.catburger.network.client.ClientPacketHandler;

import java.util.function.Supplier;

/**
 * 把接管了本次死亡的跟随者物品发给客户端，用于显示物品激活浮层。
 */
public record PlayerDeathPacket(ItemStack stack) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
    }

    public static PlayerDeathPacket decode(FriendlyByteBuf buf) {
        return new PlayerDeathPacket(buf.readItem());
    }

    public void handler(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handlerPlayerDeathPacket(this.stack());
            });
        });
        context.setPacketHandled(true);
    }
}
