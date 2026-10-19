package net.zhaiji.catburger.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhaiji.catburger.init.CatBurgerCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 跟随者渲染参数的能力承载：实例在挂载时建立并常驻，是否已由服务端同步过则由实例自己的标记表达，
 * 读取方据此回落到本机客户端配置。
 */
public class FollowerRenderDataProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    private final FollowerRenderData followerRenderData = new FollowerRenderData();
    private final LazyOptional<FollowerRenderData> optional = LazyOptional.of(() -> followerRenderData);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return CatBurgerCapabilities.FOLLOWER_RENDER.orEmpty(capability, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        // 未同步时不留任何数据，否则每个玩家与有主生物都会存一份完整标签，读回时还会把未同步误当成已同步
        if (!followerRenderData.isSynced()) return new CompoundTag();

        return followerRenderData.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        followerRenderData.deserializeNBT(nbt);
    }

    public FollowerRenderData getFollowerRenderData() {
        return followerRenderData;
    }
}
