package net.zhaiji.catburger.compat.curios;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 跟随者物品的 Curio 实现，覆写 canEquipFromUse 使物品可右键直接入槽。
 */
public class FollowerCurio implements ICurioItem {
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
