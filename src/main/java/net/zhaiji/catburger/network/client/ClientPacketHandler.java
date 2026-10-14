package net.zhaiji.catburger.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ClientPacketHandler {
    public static void handlerPlayerDeathPacket(ItemStack itemStack) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(itemStack);
    }
}
