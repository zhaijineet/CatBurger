package net.zhaiji.catburger.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.zhaiji.catburger.CatBurger;

import java.util.function.IntFunction;

/**
 * 编队模式下每一节的注视方向：全队统一看向佩戴者视线瞄准的目标，或每一节看向它前面那一节。
 */
public enum FormationGazeTarget implements StringRepresentable, TranslatableEnum {
    WEARER_VIEW("wearer_view", 0),
    FRONT_FOLLOWER("front_follower", 1);

    public static final StringRepresentable.EnumCodec<FormationGazeTarget> CODEC = StringRepresentable.fromEnum(FormationGazeTarget::values);

    public static final IntFunction<FormationGazeTarget> BY_ID = ByIdMap.continuous(FormationGazeTarget::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<ByteBuf, FormationGazeTarget> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FormationGazeTarget::getId);

    private final String name;
    private final int id;

    FormationGazeTarget(String name, int id) {
        this.name = name;
        this.id = id;
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

    @Override
    public Component getTranslatedName() {
        return Component.translatable(getTranslationKey());
    }

    private int getId() {
        return this.id;
    }
}
