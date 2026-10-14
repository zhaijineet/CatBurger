package net.zhaiji.catburger.client.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.zhaiji.catburger.client.render.CatBurgerRenderer;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.TooltipUtil;
import net.zhaiji.catburger.util.WornFollower;

import java.util.List;

public class ClientEventHandler {
    public static void handlerRenderLivingEvent$Post(RenderLivingEvent.Post event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.isSpectator()) return;

        // TODO(阶段 7.1): 过渡实现，只渲染第一件；改为遍历全部可渲染跟随者，并把"第几个 / 共几个"传给渲染器
        List<WornFollower> wornFollowers = FollowerUtil.findAll(livingEntity);
        if (wornFollowers.isEmpty() || !wornFollowers.getFirst().renderable()) return;

        CatBurgerRenderer.render(
                livingEntity,
                wornFollowers.getFirst().itemStack(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                event.getPartialTick()
        );
    }

    /**
     * 服务端配置加载、被修改、从服务器同步或卸载时清空 tooltip 缓存，使效果说明行按新配置重建。
     */
    public static void handlerModConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() != CatBurgerServerConfig.SPEC) return;
        TooltipUtil.invalidateEffectLineCache();
    }
}
