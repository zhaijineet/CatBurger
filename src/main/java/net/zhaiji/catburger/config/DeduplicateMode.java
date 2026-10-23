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
 * 同一物品戴在多个槽位时的取舍方式：关闭不去重，区分附魔光效按有无附魔光效各留一件，
 * 两个优先档位只保留对应档位的一件。
 */
public enum DeduplicateMode implements StringRepresentable, TranslatableEnum {
    OFF("off", 0),
    SPLIT_BY_GLINT("split_by_glint", 1),
    PREFER_NO_GLINT("prefer_no_glint", 2),
    PREFER_GLINT("prefer_glint", 3);

    public static final StringRepresentable.EnumCodec<DeduplicateMode> CODEC = StringRepresentable.fromEnum(DeduplicateMode::values);

    public static final IntFunction<DeduplicateMode> BY_ID = ByIdMap.continuous(DeduplicateMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<ByteBuf, DeduplicateMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DeduplicateMode::getId);

    private final String name;
    private final int id;

    DeduplicateMode(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * 按序列化名查找，名字不匹配时回落到区分附魔光效；读到的是存档或配置值，保持当前默认档。
     * 与BY_ID越界回落OFF不同是有意设计：网络流的ID越界属协议异常，保守回退到不去重不会藏起玩家实际佩戴的件。
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

    @Override
    public Component getTranslatedName() {
        return Component.translatable(getTranslationKey());
    }

    private int getId() {
        return this.id;
    }
}
