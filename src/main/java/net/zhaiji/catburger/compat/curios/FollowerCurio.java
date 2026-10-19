package net.zhaiji.catburger.compat.curios;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * 跟随者物品的 Curios 能力实现，覆写 canEquipFromUse 使物品可右键直接入槽。
 */
public record FollowerCurio(ItemStack itemStack) implements ICurio {
    @Override
    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext) {
        return true;
    }
}
