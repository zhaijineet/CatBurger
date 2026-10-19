package net.zhaiji.catburger.config;

import net.minecraft.util.StringRepresentable;
import net.zhaiji.catburger.CatBurger;

/**
 * 同一佩戴者戴多件跟随者时的排布方式：独立让每件各自漂移，编队让它们均匀环绕。
 */
public enum FollowerRenderMode implements StringRepresentable {
    INDEPENDENT("independent"),
    FORMATION("formation");

    public static final StringRepresentable.EnumCodec<FollowerRenderMode> CODEC = StringRepresentable.fromEnum(FollowerRenderMode::values);

    private final String name;

    FollowerRenderMode(String name) {
        this.name = name;
    }

    /**
     * 按序列化名查找，名字不匹配时回落到独立模式。
     */
    public static FollowerRenderMode byName(String name) {
        return CODEC.byName(name, INDEPENDENT);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 配置界面与选项列表读取显示名所用的翻译键，是它的唯一来源。
     */
    public String getTranslationKey() {
        return CatBurger.MOD_ID + ".configuration.followerRenderMode." + this.name;
    }
}
