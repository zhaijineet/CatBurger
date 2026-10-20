package net.zhaiji.catburger.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.config.FollowerConfigMode;

import java.util.function.Supplier;

public class CatBurgerDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CatBurger.MOD_ID);

    /**
     * 配置卡里存的一份渲染参数，添加模式写入、移除模式清空、右键生物时复制给目标。
     */
    public static final Supplier<DataComponentType<FollowerRenderData>> FOLLOWER_CONFIG = COMPONENT_TYPES.register(
            "follower_config",
            () -> DataComponentType.<FollowerRenderData>builder()
                                    .persistent(FollowerRenderData.CODEC)
                                    .networkSynchronized(FollowerRenderData.STREAM_CODEC)
                                    .build()
    );

    /**
     * 配置卡的当前模式，滚轮切换后随物品同步。
     */
    public static final Supplier<DataComponentType<FollowerConfigMode>> FOLLOWER_CONFIG_MODE = COMPONENT_TYPES.register(
            "follower_config_mode",
            () -> DataComponentType.<FollowerConfigMode>builder()
                                    .persistent(FollowerConfigMode.CODEC)
                                    .networkSynchronized(FollowerConfigMode.STREAM_CODEC)
                                    .build()
    );
}
