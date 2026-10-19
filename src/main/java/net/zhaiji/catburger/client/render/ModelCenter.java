package net.zhaiji.catburger.client.render;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.phys.Vec3;

/**
 * 物品模型的几何中心缓存条目：同时记录算中心所用的模型实例，模型实例变化时调用方据此重算。
 */
public record ModelCenter(BakedModel model, Vec3 center) {
}
