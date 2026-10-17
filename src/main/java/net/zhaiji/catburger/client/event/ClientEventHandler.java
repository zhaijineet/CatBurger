package net.zhaiji.catburger.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.zhaiji.catburger.client.render.FollowerRenderer;
import net.zhaiji.catburger.client.render.RenderParamsResolver;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.network.client.ClientPacketHandler;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.TooltipUtil;
import net.zhaiji.catburger.util.WornFollower;

import java.util.List;

public class ClientEventHandler {
    public static void handlerRenderLivingEvent$Post(RenderLivingEvent.Post event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.isSpectator()) return;

        List<WornFollower> wornFollowers = FollowerUtil.findAll(livingEntity);
        if (wornFollowers.isEmpty()) return;

        FollowerRenderer.renderAll(
                livingEntity,
                wornFollowers,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                event.getPartialTick()
        );
    }

    /**
     * 第一人称下摄像机实体本身不参与实体渲染，RenderLivingEvent 不会触发，在实体阶段结束后为它补渲染跟随者；
     * 第三人称、睡眠时它照常渲染，交给 RenderLivingEvent，避免画两遍。
     */
    public static void handlerRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Camera camera = event.getCamera();
        if (camera.isDetached() || !(camera.getEntity() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.isSleeping() || livingEntity.isSpectator()) return;

        List<WornFollower> wornFollowers = FollowerUtil.findAll(livingEntity);
        if (wornFollowers.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(
                !livingEntity.level().tickRateManager().isEntityFrozen(livingEntity)
        );
        Vec3 cameraPosition = camera.getPosition();
        Vec3 entityPosition = livingEntity.getPosition(partialTicks);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                entityPosition.x - cameraPosition.x,
                entityPosition.y - cameraPosition.y,
                entityPosition.z - cameraPosition.z
        );
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        FollowerRenderer.renderAll(
                livingEntity,
                wornFollowers,
                poseStack,
                bufferSource,
                minecraft.getEntityRenderDispatcher().getPackedLightCoords(livingEntity, partialTicks),
                partialTicks
        );
        poseStack.popPose();
        bufferSource.endLastBatch();
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
            RenderParamsResolver.invalidateLocalFallback();
            ClientPacketHandler.sendFollowerRenderData();
        }
    }
}
