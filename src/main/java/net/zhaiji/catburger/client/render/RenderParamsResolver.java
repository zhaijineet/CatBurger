package net.zhaiji.catburger.client.render;

import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.EntityDataFallback;
import net.zhaiji.catburger.util.FollowerUtil;

/**
 * 取某个佩戴者的渲染参数：已由服务端同步过就用同步值，否则按配置项回退到本地配置当前值或配置默认值。
 * 有主生物的同步值由服务端从主人那里复制过来，因此这里不需要再关心主人是谁、在不在场。
 */
public class RenderParamsResolver {
    private static FollowerRenderData localFallback;

    public static FollowerRenderData resolve(LivingEntity livingEntity) {
        FollowerRenderData attachedData = FollowerUtil.followerRenderDataOf(livingEntity);
        if (attachedData != null && attachedData.isSynced()) return attachedData;

        return CatBurgerClientConfig.ENTITY_DATA_FALLBACK.get() == EntityDataFallback.CONFIG_DEFAULT
                ? FollowerRenderData.CONFIG_DEFAULTS
                : localFallback();
    }

    /**
     * 客户端配置重载后调用，使下一次解析按新配置重建本机回退参数。
     */
    public static void invalidateLocalFallback() {
        localFallback = null;
    }

    /**
     * 字段只读一次进局部变量，避免判空与返回之间被清空导致返回null。
     */
    private static FollowerRenderData localFallback() {
        FollowerRenderData renderParams = localFallback;
        if (renderParams == null) {
            renderParams = FollowerRenderData.fromClientConfig();
            localFallback = renderParams;
        }

        return renderParams;
    }
}
