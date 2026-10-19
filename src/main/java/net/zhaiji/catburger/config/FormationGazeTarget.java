package net.zhaiji.catburger.config;

import net.minecraft.util.StringRepresentable;
import net.zhaiji.catburger.CatBurger;

/**
 * 编队模式下每一节的注视方向：全队统一看向佩戴者视线瞄准的目标，或每一节看向它前面那一节。
 */
public enum FormationGazeTarget implements StringRepresentable {
    WEARER_VIEW("wearer_view"),
    FRONT_FOLLOWER("front_follower");

    public static final StringRepresentable.EnumCodec<FormationGazeTarget> CODEC = StringRepresentable.fromEnum(FormationGazeTarget::values);

    private final String name;

    FormationGazeTarget(String name) {
        this.name = name;
    }

    /**
     * 按序列化名查找，名字不匹配时回落到跟随佩戴者视线。
     */
    public static FormationGazeTarget byName(String name) {
        return CODEC.byName(name, WEARER_VIEW);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 配置界面与选项列表读取显示名所用的翻译键，是它的唯一来源。
     */
    public String getTranslationKey() {
        return CatBurger.MOD_ID + ".configuration.formationGazeTarget." + this.name;
    }
}
