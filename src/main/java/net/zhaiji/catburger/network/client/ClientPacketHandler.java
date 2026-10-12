package net.zhaiji.catburger.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.zhaiji.catburger.init.CatBurgerItems;

public class ClientPacketHandler {
    public static void handlerPlayerDeathPacket(Player player) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(CatBurgerItems.CAT_BURGER.get().getDefaultInstance());
    }
}
