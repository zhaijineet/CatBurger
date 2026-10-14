package net.zhaiji.catburger.util;

import net.minecraft.world.item.ItemStack;

/**
 * 一件已佩戴的跟随者：物品本体、所属槽位的身份键（由产生方给出，原版头盔槽为 equipment:head，
 * Curios 槽为 curios:槽标识:补零索引，两个键空间互不冲突），以及该槽位是否参与渲染。
 */
public record WornFollower(ItemStack itemStack, String slotKey, boolean renderable) {
}
