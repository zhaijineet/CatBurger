package net.zhaiji.catburger.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.init.CatBurgerDataComponents;

import java.util.function.IntFunction;

/**
 * 追随者配置卡的三个模式：添加把卡里的参数写进目标，移除把目标的参数清掉，穿戴在右键时给目标戴上追随者、Shift右键时取下。
 */
public enum FollowerConfigMode implements StringRepresentable {
    ADD("add", 0, ChatFormatting.GREEN),
    REMOVE("remove", 1, ChatFormatting.RED),
    WEAR("wear", 2, ChatFormatting.LIGHT_PURPLE);

    public static final StringRepresentable.EnumCodec<FollowerConfigMode> CODEC = StringRepresentable.fromEnum(FollowerConfigMode::values);

    public static final IntFunction<FollowerConfigMode> BY_ID = ByIdMap.continuous(FollowerConfigMode::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<ByteBuf, FollowerConfigMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FollowerConfigMode::getId);

    private final String name;
    private final int id;
    private final ChatFormatting color;

    FollowerConfigMode(String name, int id, ChatFormatting color) {
        this.name = name;
        this.id = id;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 读取物品上保存的模式，未设置时按添加模式处理。
     */
    public static FollowerConfigMode getMode(ItemStack itemStack) {
        return itemStack.getOrDefault(CatBurgerDataComponents.FOLLOWER_CONFIG_MODE.get(), ADD);
    }

    /**
     * 切换提示与物品 tooltip 里给模式名着色所用的颜色。
     */
    public ChatFormatting getColor() {
        return this.color;
    }

    private int getId() {
        return this.id;
    }
}
