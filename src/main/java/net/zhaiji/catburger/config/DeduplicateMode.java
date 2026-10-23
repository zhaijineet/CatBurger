package net.zhaiji.catburger.config;

import net.minecraft.util.StringRepresentable;
import net.zhaiji.catburger.CatBurger;

/**
 * 同一物品戴在多个槽位时的取舍方式：关闭不去重，区分附魔光效按有无附魔光效各留一件，
 * 两个优先档位只保留对应档位的一件。
 */
public enum DeduplicateMode implements StringRepresentable {
    OFF("off"),
    SPLIT_BY_GLINT("split_by_glint"),
    PREFER_NO_GLINT("prefer_no_glint"),
    PREFER_GLINT("prefer_glint");

    public static final StringRepresentable.EnumCodec<DeduplicateMode> CODEC = StringRepresentable.fromEnum(DeduplicateMode::values);

    private final String name;

    DeduplicateMode(String name) {
        this.name = name;
    }

    /**
     * 按序列化名查找，名字不匹配时回落到区分附魔光效；读到的是存档或配置值，保持当前默认档。
     */
    public static DeduplicateMode byName(String name) {
        return CODEC.byName(name, SPLIT_BY_GLINT);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 配置界面与选项列表读取显示名所用的翻译键，是它的唯一来源。
     */
    public String getTranslationKey() {
        return CatBurger.MOD_ID + ".configuration.deduplicateMode." + this.name;
    }
}
