package net.zhaiji.catburger.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zhaiji.catburger.client.render.FollowerRenderer;
import net.zhaiji.catburger.client.render.RenderParamsResolver;
import net.zhaiji.catburger.compat.cloth.ClothConfigCompat;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.client.ClientPacketHandler;
import net.zhaiji.catburger.network.server.packet.SetConfigCardModePacket;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.TooltipUtil;
import net.zhaiji.catburger.util.WornFollower;

import java.util.List;

public class ClientEventHandler {
    /**
     * 到这一步才用 ModList 精判 Cloth Config 是否真的加载，文件被发现不等于加载成功。
     * 配置界面是点击时才创建，此时注册不影响它在模组列表里的出现时机。
     */
    public static void handlerFMLClientSetupEvent(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("cloth_config")) ClothConfigCompat.registerConfigScreen();
    }

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
        if (!CatBurgerClientConfig.SHOW_IN_FIRST_PERSON.get()) return;
        if (livingEntity.isSleeping() || livingEntity.isSpectator()) return;

        List<WornFollower> wornFollowers = FollowerUtil.findAll(livingEntity);
        if (wornFollowers.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = event.getPartialTick();
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
     * 资源重载会重建烘焙模型，跟随者按物品缓存的几何中心随之失效，这里挂上清空用的监听器。
     */
    public static void handlerRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> FollowerRenderer.invalidateModelCenters());
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
     * 配置监听来自文件监听线程，客户端分支必须回到主线程，发包与渲染参数都只在主线程动。
     */
    public static void handlerModConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == CatBurgerServerConfig.SPEC) TooltipUtil.invalidateEffectLineCache();
        if (event.getConfig().getSpec() == CatBurgerClientConfig.SPEC) {
            Minecraft minecraft = Minecraft.getInstance();
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

        double scrollDelta = event.getScrollDelta();
        if (scrollDelta == 0) return;

        PacketManager.sendToServer(new SetConfigCardModePacket(scrollDelta > 0));
        event.setCanceled(true);
    }
}
