package net.zhaiji.catburger.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.server.packet.SyncFollowerRenderDataPacket;
import net.zhaiji.catburger.util.FollowerUtil;

import java.util.UUID;

public class ClientPacketHandler {
    public static void handlerPlayerDeathPacket(ItemStack itemStack) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(itemStack);
    }

    /**
     * 把本机渲染参数上传给服务端，未连接服务器时跳过。
     */
    public static void sendFollowerRenderData() {
        if (Minecraft.getInstance().getConnection() == null) return;

        FollowerRenderData followerRenderData = new FollowerRenderData();
        followerRenderData.loadFromClientConfig();
        PacketManager.sendToServer(new SyncFollowerRenderDataPacket(followerRenderData));
    }

    /**
     * 把服务端广播来的参数写入该实体在客户端的能力实例，渲染时按与1.21.1相同的入口读取；
     * 载荷也覆盖有主生物，因此先按玩家找，找不到再按UUID在已加载实体里找。
     */
    public static void handlerClientboundFollowerRenderDataPacket(UUID entityId, FollowerRenderData followerRenderData) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;

        Player player = clientLevel.getPlayerByUUID(entityId);
        LivingEntity wearer = player == null ? findEntityById(clientLevel, entityId) : player;
        if (wearer == null) return;

        FollowerRenderData clientData = FollowerUtil.followerRenderDataOf(wearer);
        if (clientData != null) clientData.deserializeNBT(followerRenderData.serializeNBT());
    }

    /**
     * 客户端只有玩家表可以直接按UUID查，其余实体逐一遍历已加载实体。
     */
    private static LivingEntity findEntityById(ClientLevel clientLevel, UUID entityId) {
        for (Entity entity : clientLevel.entitiesForRendering()) {
            if (!entityId.equals(entity.getUUID())) continue;
            if (entity instanceof LivingEntity livingEntity) return livingEntity;
        }

        return null;
    }
}
