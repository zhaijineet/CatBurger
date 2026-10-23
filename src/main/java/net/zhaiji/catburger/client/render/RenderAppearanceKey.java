package net.zhaiji.catburger.client.render;

import net.minecraft.client.resources.model.BakedModel;

import java.util.List;

/**
 * 一件跟随者在画面上呈现的外观：模型实例与模型各染色索引对应的颜色，
 * 模型与全部颜色都相同才视为同一件。
 */
public record RenderAppearanceKey(BakedModel model, List<Integer> colors) {
}
