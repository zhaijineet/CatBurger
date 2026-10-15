package net.zhaiji.catburger.network.client.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.network.client.ClientPacketHandler;

/**
 * 把接管了本次死亡的跟随者物品发给客户端，用于显示物品激活浮层。
 */
public record PlayerDeathPacket(ItemStack stack) implements CustomPacketPayload {
    public static final Type<PlayerDeathPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "player_death_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDeathPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            PlayerDeathPacket::stack,
            PlayerDeathPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(PlayerDeathPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.handlerPlayerDeathPacket(packet.stack()));
    }
}
