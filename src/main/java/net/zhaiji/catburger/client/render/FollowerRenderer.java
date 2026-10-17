package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.util.WornFollower;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FollowerRenderer {
    private static final double MIN_DISPLAY_SIZE = 0.1;

    // 背包等界面预览期间为 true，由 InventoryScreenMixin 在 renderEntityInInventory 前后置位与复位
    private static boolean renderingInventoryPreview;

    public static void beginInventoryPreview() {
        renderingInventoryPreview = true;
    }

    public static void endInventoryPreview() {
        renderingInventoryPreview = false;
    }

    /**
     * 渲染一名佩戴者身上的全部可渲染跟随者：去重后求一份散布布局并逐件渲染；编队模式在阶段5 接管前仍只渲染首件。
     */
    public static void renderAll(
            LivingEntity livingEntity,
            List<WornFollower> wornFollowers,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        if (wornFollowers.isEmpty()) return;

        FollowerRenderData renderParams = RenderParamsResolver.resolve(livingEntity);
        List<WornFollower> renderedFollowers = selectRendered(livingEntity, wornFollowers, renderParams);
        if (renderedFollowers.isEmpty()) return;

        double displaySize = MIN_DISPLAY_SIZE;
        for (WornFollower wornFollower : renderedFollowers) {
            displaySize = Math.max(displaySize, displaySizeOf(livingEntity, wornFollower.itemStack(), renderParams));
        }

        if (renderParams.getRenderMode() == FollowerRenderMode.FORMATION) {
            if (renderingInventoryPreview) {
                double[] lineLayout = FollowerLayout.line(renderedFollowers.size(), renderParams.getFormationSpacing());
                for (int index = 0; index < renderedFollowers.size(); index++) {
                    renderOne(
                            livingEntity,
                            renderedFollowers.get(index),
                            lineLayout[index * 3],
                            lineLayout[index * 3 + 1],
                            lineLayout[index * 3 + 2],
                            renderParams,
                            poseStack,
                            buffer,
                            packedLight,
                            partialTicks
                    );
                }
            } else {
                renderFormation(livingEntity, renderedFollowers, renderParams, displaySize, poseStack, buffer, packedLight, partialTicks);
            }
            return;
        }

        double[] layout = FollowerLayout.scatter(livingEntity, renderedFollowers, renderParams, displaySize);
        for (int index = 0; index < renderedFollowers.size(); index++) {
            renderOne(
                    livingEntity,
                    renderedFollowers.get(index),
                    layout[index * 3],
                    layout[index * 3 + 1],
                    layout[index * 3 + 2],
                    renderParams,
                    poseStack,
                    buffer,
                    packedLight,
                    partialTicks
            );
        }
    }

    /**
     * 编队模式：首件按自身状态渲染，其后各节取尾巴的世界坐标与朝向，缩放沿用首件。
     */
    private static void renderFormation(
            LivingEntity livingEntity,
            List<WornFollower> renderedFollowers,
            FollowerRenderData renderParams,
            double displaySize,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        FollowerState followerState = FollowerState.of(livingEntity, FollowerState.FORMATION_KEY, renderParams);
        followerState.updateFormation(
                renderParams,
                renderedFollowers.size() - 1,
                renderParams.getFormationSpacing(),
                displaySize
        );
        float scale = followerState.scale();
        draw(
                livingEntity,
                renderedFollowers.getFirst().itemStack(),
                followerState.xOffset(partialTicks),
                followerState.yOffset(partialTicks),
                followerState.zOffset(partialTicks),
                followerState.renderYaw(partialTicks),
                followerState.headPitch(partialTicks),
                followerState.gazeYaw(partialTicks),
                scale,
                poseStack,
                buffer,
                packedLight
        );
        for (int index = 1; index < renderedFollowers.size(); index++) {
            int segment = index - 1;
            draw(
                    livingEntity,
                    renderedFollowers.get(index).itemStack(),
                    followerState.tailXOffset(segment, partialTicks),
                    followerState.tailYOffset(segment, partialTicks),
                    followerState.tailZOffset(segment, partialTicks),
                    followerState.tailYaw(segment, partialTicks),
                    followerState.tailPitch(segment, partialTicks),
                    followerState.tailGazeYaw(segment, partialTicks),
                    scale,
                    poseStack,
                    buffer,
                    packedLight
            );
        }
    }

    /**
     * 取参与渲染的跟随者：跳过不可渲染的槽位，开启去重时按“物品 + 解析出的模型实例”只保留顺序最靠前的一件。
     * 已知局限：只换颜色不换模型会被误合并；资源重载后模型实例重建，跨重载不可比。
     */
    private static List<WornFollower> selectRendered(
            LivingEntity livingEntity,
            List<WornFollower> wornFollowers,
            FollowerRenderData renderParams
    ) {
        List<WornFollower> renderedFollowers = new ArrayList<>(wornFollowers.size());
        Set<RenderedModelKey> renderedModels = new HashSet<>();
        for (WornFollower wornFollower : wornFollowers) {
            if (!wornFollower.renderable()) continue;
            if (renderParams.isDeduplicateRendering()) {
                ItemStack itemStack = wornFollower.itemStack();
                BakedModel model = Minecraft.getInstance().getItemRenderer()
                        .getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId());
                if (!renderedModels.add(new RenderedModelKey(itemStack.getItem(), model))) continue;
            }

            renderedFollowers.add(wornFollower);
        }

        return renderedFollowers;
    }

    /**
     * 估算跟随者在世界中的显示边长：模型头部显示变换的最大缩放乘以渲染缩放，物品模型按一格方块计。
     */
    private static double displaySizeOf(LivingEntity livingEntity, ItemStack itemStack, FollowerRenderData renderParams) {
        BakedModel model = Minecraft.getInstance().getItemRenderer()
                .getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId());
        PoseStack transformStack = new PoseStack();
        model.applyTransform(ItemDisplayContext.HEAD, transformStack, false);
        Vector3f headScale = transformStack.last().pose().getScale(new Vector3f());
        float modelScale = Math.max(headScale.x(), Math.max(headScale.y(), headScale.z()));
        return Math.max(modelScale * FollowerState.renderScale(livingEntity, renderParams), MIN_DISPLAY_SIZE);
    }

    /**
     * 渲染单件跟随者：世界路径按槽位状态推进并取位置与朝向，预览路径取界面改写后的视角与无状态位置。
     */
    private static void renderOne(
            LivingEntity livingEntity,
            WornFollower wornFollower,
            double layoutForward,
            double layoutRight,
            double layoutUp,
            FollowerRenderData renderParams,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        double xOffset;
        double yOffset;
        double zOffset;
        float renderYaw;
        float headPitch;
        float gazeYaw;
        float scale;
        if (renderingInventoryPreview) {
            float viewYaw = livingEntity.getViewYRot(partialTicks);
            xOffset = FollowerState.previewXOffset(livingEntity, renderParams, viewYaw, layoutForward, layoutRight);
            yOffset = FollowerState.previewYOffset(livingEntity, renderParams, partialTicks, layoutUp);
            zOffset = FollowerState.previewZOffset(livingEntity, renderParams, viewYaw, layoutForward, layoutRight);
            renderYaw = viewYaw;
            headPitch = livingEntity.getViewXRot(partialTicks);
            gazeYaw = 0.0F;
            scale = FollowerState.previewScale(livingEntity, renderParams);
        } else {
            FollowerState followerState = FollowerState.of(livingEntity, wornFollower.slotKey(), renderParams);
            followerState.update(renderParams, layoutForward, layoutRight, layoutUp);
            xOffset = followerState.xOffset(partialTicks);
            yOffset = followerState.yOffset(partialTicks);
            zOffset = followerState.zOffset(partialTicks);
            renderYaw = followerState.renderYaw(partialTicks);
            headPitch = followerState.headPitch(partialTicks);
            gazeYaw = followerState.gazeYaw(partialTicks);
            scale = followerState.scale();
        }

        draw(
                livingEntity,
                wornFollower.itemStack(),
                xOffset,
                yOffset,
                zOffset,
                renderYaw,
                headPitch,
                gazeYaw,
                scale,
                poseStack,
                buffer,
                packedLight
        );
    }

    private static void draw(
            LivingEntity livingEntity,
            ItemStack itemStack,
            double xOffset,
            double yOffset,
            double zOffset,
            float renderYaw,
            float headPitch,
            float gazeYaw,
            float scale,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.scale(scale, scale, scale);
        // 模型正面朝北（-Z）建模，偏航按 (180 - 偏航) 换算，模型自身不再做任何翻转
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - renderYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-headPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-gazeYaw));
        // seed 与 CustomHeadLayer 一致：entityId + 头部显示上下文的序号
        Minecraft.getInstance().getItemRenderer().renderStatic(
                livingEntity,
                itemStack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                livingEntity.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                livingEntity.getId() + ItemDisplayContext.HEAD.ordinal()
        );
        poseStack.popPose();
    }

    private record RenderedModelKey(Item item, BakedModel model) {
    }
}
