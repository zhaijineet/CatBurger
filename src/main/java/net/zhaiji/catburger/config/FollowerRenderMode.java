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
 * 同一佩戴者戴多件跟随者时的排布方式：独立让每件各自漂移，编队让它们均匀环绕。
 */
public enum FollowerRenderMode implements StringRepresentable, TranslatableEnum {
    INDEPENDENT("independent", 0),
    FORMATION("formation", 1);

    public static final StringRepresentable.EnumCodec<FollowerRenderMode> CODEC = StringRepresentable.fromEnum(FollowerRenderMode::values);

    public static final IntFunction<FollowerRenderMode> BY_ID = ByIdMap.continuous(FollowerRenderMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<ByteBuf, FollowerRenderMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FollowerRenderMode::getId);

    private final String name;
    private final int id;

    FollowerRenderMode(String name, int id) {
        this.name = name;
        this.id = id;
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

    @Override
    public Component getTranslatedName() {
        return Component.translatable(getTranslationKey());
    }

    private int getId() {
        return this.id;
    }
}
