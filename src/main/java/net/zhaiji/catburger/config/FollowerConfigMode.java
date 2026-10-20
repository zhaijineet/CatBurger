package net.zhaiji.catburger.config;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * 追随者配置卡的三个模式：添加把卡里的参数写进目标，移除把目标的参数清掉，穿戴在右键时给目标戴上追随者、Shift右键时取下。
 */
public enum FollowerConfigMode implements StringRepresentable {
    ADD("add", ChatFormatting.GREEN),
    REMOVE("remove", ChatFormatting.RED),
    WEAR("wear", ChatFormatting.LIGHT_PURPLE);

    public static final StringRepresentable.EnumCodec<FollowerConfigMode> CODEC = StringRepresentable.fromEnum(FollowerConfigMode::values);

    private final String name;
    private final ChatFormatting color;

    FollowerConfigMode(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    /**
     * 按序列化名查找，名字不匹配时回落到添加模式。
     */
    public static FollowerConfigMode byName(String name) {
        return CODEC.byName(name, ADD);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 切换提示与物品 tooltip 里给模式名着色所用的颜色。
     */
    public ChatFormatting getColor() {
        return this.color;
    }
}
