package net.zhaiji.catburger.client.event;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.zhaiji.catburger.client.render.FirstPersonFollowerHolder;
import net.zhaiji.catburger.client.render.FollowerRenderer;
import net.zhaiji.catburger.client.render.RenderParamsResolver;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.network.client.ClientPacketHandler;
import net.zhaiji.catburger.network.server.packet.SetConfigCardModePacket;
import net.zhaiji.catburger.util.TooltipUtil;

public class ClientEventHandler {
    /**
     * 提交阶段拿不到实体，跟随者数据只能由抽取阶段算好写进渲染状态，因此这里挂上抽取用的修饰器。
     */
    public static void handlerRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {
                },
                FollowerRenderer::attachWearer
        );
    }

    public static void handlerRenderLivingEvent$Post(RenderLivingEvent.Post<?, ?, ?> event) {
        FollowerRenderer.renderAll(
                event.getRenderState(),
                event.getRenderState().lightCoords,
                event.getRenderState().outlineColor,
                event.getPoseStack(),
                event.getSubmitNodeCollector()
        );
    }

    /**
     * 第一人称下摄像机实体本身不参与实体渲染，RenderLivingEvent 不会触发，这里在抽取阶段为它现抽一份渲染状态，
     * 连同按原版取的亮度一并存进关卡渲染状态，供提交阶段补渲染使用。
     */
    public static void handlerExtractLevelRenderStateEvent(ExtractLevelRenderStateEvent event) {
        FirstPersonFollowerHolder levelRenderStateHolder = (FirstPersonFollowerHolder) event.getRenderState();
        // 关卡渲染状态跨帧复用，先清空，使本帧不补渲染时不会沿用上一帧的数据
        levelRenderStateHolder.catburger$setFirstPersonRenderState(null);

        Camera camera = event.getCamera();
        if (camera.isDetached() || !(camera.entity() instanceof LivingEntity livingEntity)) return;
        if (!CatBurgerClientConfig.SHOW_IN_FIRST_PERSON.get()) return;
        if (livingEntity.isSleeping() || livingEntity.isSpectator()) return;

        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = event.getDeltaTracker()
                .getGameTimeDeltaPartialTick(!livingEntity.level().tickRateManager().isEntityFrozen(livingEntity));
        EntityRenderer<? super LivingEntity, ?> entityRenderer = minecraft.getEntityRenderDispatcher().getRenderer(livingEntity);
        EntityRenderState renderState = entityRenderer.createRenderState(livingEntity, partialTicks);
        if (!(renderState instanceof LivingEntityRenderState livingRenderState)) return;

        livingRenderState.lightCoords = minecraft.getEntityRenderDispatcher().getPackedLightCoords(livingEntity, partialTicks);
        levelRenderStateHolder.catburger$setFirstPersonRenderState(livingRenderState);
    }

    /**
     * 补渲染只读抽取阶段存下的渲染状态，描边与实体阶段无关故不描边。
     */
    public static void handlerSubmitCustomGeometryEvent(SubmitCustomGeometryEvent event) {
        LivingEntityRenderState renderState = ((FirstPersonFollowerHolder) event.getLevelRenderState()).catburger$firstPersonRenderState();
        if (renderState == null) return;

        Vec3 cameraPosition = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                renderState.x - cameraPosition.x,
                renderState.y - cameraPosition.y,
                renderState.z - cameraPosition.z
        );
        FollowerRenderer.renderAll(
                renderState,
                renderState.lightCoords,
                EntityRenderState.NO_OUTLINE,
                poseStack,
                event.getSubmitNodeCollector()
        );
        poseStack.popPose();
    }

    /**
     * 登录完成后把本机渲染参数上传给服务端，使其他玩家按本机设置看到自己的跟随者。
     */
    public static void handlerClientPlayerNetworkEvent$LoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPacketHandler.sendFollowerRenderData();
    }

    /**
     * 服务端配置加载、被修改、从服务器同步或卸载时清空 tooltip 缓存，使效果说明行按新配置重建；
     * 客户端配置被修改时丢弃按旧配置构造的本机回退渲染参数，并把新参数上传给服务端。
     */
    public static void handlerModConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == CatBurgerServerConfig.SPEC) TooltipUtil.invalidateEffectLineCache();
        if (event.getConfig().getSpec() == CatBurgerClientConfig.SPEC) {
            Minecraft minecraft = Minecraft.getInstance();
            // 26.1 的客户端配置加载发生在 Minecraft 构造之前，此时没有实例可派发，跳过；
            // 回退参数按首次访问懒加载，参数上传由登录事件兜底
            if (minecraft == null) return;
            minecraft.execute(() -> {
                RenderParamsResolver.invalidateLocalFallback();
                ClientPacketHandler.sendFollowerRenderData();
            });
        }
    }

    /**
     * 按住Shift滚轮在配置卡的三个模式间循环切换，拦截滚轮避免切换物品栏，并上报服务端同步服务端那份物品栈。
     */
    public static void handlerMouseScrollingEvent(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) return;

        Player player = minecraft.player;
        if (!player.isShiftKeyDown()) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(CatBurgerItems.FOLLOWER_CONFIG_CARD.get())) return;

        double scrollDeltaY = event.getScrollDeltaY();
        if (scrollDeltaY == 0) return;

        ClientPacketDistributor.sendToServer(new SetConfigCardModePacket(scrollDeltaY > 0));
        event.setCanceled(true);
    }
}
