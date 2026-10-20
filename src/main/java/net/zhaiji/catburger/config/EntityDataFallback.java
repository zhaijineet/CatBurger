package net.zhaiji.catburger.config;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.zhaiji.catburger.CatBurger;

/**
 * 实体没有渲染参数附件时的回退来源：本地配置的当前值，或配置声明的默认值。
 */
public enum EntityDataFallback implements StringRepresentable, TranslatableEnum {
    LOCAL("local"),
    CONFIG_DEFAULT("config_default");

    private final String name;

    EntityDataFallback(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 配置界面与选项列表读取显示名所用的翻译键，是它的唯一来源。
     */
    public String getTranslationKey() {
        return CatBurger.MOD_ID + ".configuration.entityDataFallback." + this.name;
    }

    @Override
    public Component getTranslatedName() {
        return Component.translatable(getTranslationKey());
    }
}
