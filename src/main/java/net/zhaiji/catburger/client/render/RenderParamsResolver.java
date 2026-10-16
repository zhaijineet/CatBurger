package net.zhaiji.catburger.client.render;

import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.init.CatBurgerAttachmentTypes;

/**
 * 取某个佩戴者的渲染参数：持有附件的实体用附件里的值，其余实体（服务端不为其写附件的女仆等）回落到本机客户端配置。
 */
public class RenderParamsResolver {
    private static FollowerRenderData localFallback;

    public static FollowerRenderData resolve(LivingEntity livingEntity) {
        FollowerRenderData attachmentData = livingEntity.getExistingDataOrNull(CatBurgerAttachmentTypes.FOLLOWER_RENDER.get());
        if (attachmentData != null) return attachmentData;

        return localFallback();
    }

    /**
     * 客户端配置重载后调用，使下一次解析按新配置重建本机回退参数。
     */
    public static void invalidateLocalFallback() {
        localFallback = null;
    }

    /**
     * 字段只读一次进局部变量，避免配置监听线程在判空与返回之间清空缓存导致返回 null。
     */
    private static FollowerRenderData localFallback() {
        FollowerRenderData renderParams = localFallback;
        if (renderParams == null) {
            renderParams = new FollowerRenderData();
            renderParams.loadFromClientConfig();
            localFallback = renderParams;
        }

        return renderParams;
    }
}
