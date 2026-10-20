package net.zhaiji.catburger.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.config.FollowerConfigMode;

/**
 * 配置卡的两份数据在1.20.1没有物品组件，改存物品自身的NBT：卡里保存的渲染参数与当前模式。
 */
public class FollowerConfigCardData {
    private static final String CONFIG_TAG = "follower_config";
    private static final String MODE_TAG = "follower_config_mode";

    /**
     * 读卡里保存的渲染参数，没存过时返回null。
     */
    public static FollowerRenderData getConfig(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(CONFIG_TAG)) return null;

        FollowerRenderData configData = new FollowerRenderData();
        configData.deserializeNBT(tag.getCompound(CONFIG_TAG));
        return configData;
    }

    /**
     * 把一份渲染参数写进卡里，写入的是本次序列化出的独立标签，卡片不与调用方共享实例。
     */
    public static void setConfig(ItemStack itemStack, FollowerRenderData configData) {
        itemStack.getOrCreateTag().put(CONFIG_TAG, configData.serializeNBT());
    }

    /**
     * 清空卡里保存的渲染参数，模式保持不变。
     */
    public static void removeConfig(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return;

        tag.remove(CONFIG_TAG);
    }

    /**
     * 读卡上的模式，未设置时按添加模式处理。
     */
    public static FollowerConfigMode getMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return FollowerConfigMode.ADD;

        return FollowerConfigMode.byName(tag.getString(MODE_TAG));
    }

    public static void setMode(ItemStack itemStack, FollowerConfigMode mode) {
        itemStack.getOrCreateTag().putString(MODE_TAG, mode.getSerializedName());
    }
}
