package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.WornFollower;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FollowerRenderer {
    // 背包等界面预览期间的佩戴者，由 InventoryScreenMixin 在抽取预览渲染状态的那个方法前后置位与复位，
    // 状态抽取发生在该方法内，因此抽取时读到的就是本次预览的佩戴者；
    // 它只决定状态取哪一份、partialTicks怎么取、是否消费位移与解析准心，跟随逻辑本身是同一套
    @Nullable
    private static LivingEntity previewWearer;

    public static void beginInventoryPreview(LivingEntity wearer) {
        previewWearer = wearer;
    }

    public static void endInventoryPreview() {
        previewWearer = null;
    }

    /**
     * 抽取渲染状态时调用：把本帧要用的跟随者数据一次算好写进状态，提交阶段只读状态，不回查实体。
     */
    public static void attachWearer(LivingEntity livingEntity, LivingEntityRenderState renderState) {
        FollowerRenderStateHolder renderStateHolder = (FollowerRenderStateHolder) renderState;
        boolean preview = livingEntity == previewWearer;
        FollowerRenderData renderParams = RenderParamsResolver.resolve(livingEntity);
        // 原版预览传入的partialTicks恒为1.0，直接沿用会让状态按刻跳变，改取与世界渲染相同的帧间比例
        float partialTicks = renderState.partialTick;
        if (preview) {
            partialTicks = Minecraft.getInstance().getDeltaTracker()
                    .getGameTimeDeltaPartialTick(!livingEntity.level().tickRateManager().isEntityFrozen(livingEntity));
        }

        renderStateHolder.catburger$setPreview(preview);
        renderStateHolder.catburger$setPartialTick(partialTicks);
        renderStateHolder.catburger$setX(livingEntity.getX());
        renderStateHolder.catburger$setY(livingEntity.getY());
        renderStateHolder.catburger$setZ(livingEntity.getZ());
        renderStateHolder.catburger$setTickCount(livingEntity.tickCount);
        renderStateHolder.catburger$setEntityScale(livingEntity.getScale());
        renderStateHolder.catburger$setEntityBbWidth(livingEntity.getBbWidth());
        renderStateHolder.catburger$setRenderParams(renderParams);
        renderStateHolder.catburger$setHeadIsFollower(FollowerUtil.isFollower(livingEntity.getItemBySlot(EquipmentSlot.HEAD)));

        List<WornFollower> wornFollowers = livingEntity.isSpectator() ? List.of() : FollowerUtil.findAll(livingEntity);
        List<WornFollower> renderedFollowers = selectRendered(livingEntity, renderState, wornFollowers, renderParams, preview);
        List<FollowerState> followerStates = new ArrayList<>(renderedFollowers.size());
        double displaySize = FollowerState.MIN_DISPLAY_SIZE;
        for (WornFollower wornFollower : renderedFollowers) {
            FollowerState followerState = FollowerState.of(livingEntity, renderState, wornFollower.slotKey(), renderParams, preview);
            followerStates.add(followerState);
            displaySize = Math.max(displaySize, followerState.itemDisplaySize());
        }
        renderStateHolder.catburger$setFollowerStates(followerStates);
        renderStateHolder.catburger$setDisplaySize(displaySize);

        if (renderParams.getRenderMode() == FollowerRenderMode.FORMATION) {
            renderStateHolder.catburger$setFormationState(
                    FollowerState.of(livingEntity, renderState, FollowerState.FORMATION_KEY, renderParams, preview)
            );
            return;
        }

        FollowerStateHolder followerStateHolder = (FollowerStateHolder) livingEntity;
        double[] layout = followerStateHolder.catburger$layout();
        // 去重开关或资源重载会改变参与渲染的件数，长度不符时必须重算，否则逐件取偏移会越界
        if (followerStateHolder.catburger$layoutParams() != renderParams
                || followerStateHolder.catburger$layoutDisplaySize() != displaySize
                || layout.length != renderedFollowers.size() * 3) {
            layout = FollowerLayout.scatter(livingEntity, renderedFollowers, renderParams, displaySize);
            followerStateHolder.catburger$setLayout(renderParams, displaySize, layout);
        }
        renderStateHolder.catburger$setLayout(layout);
    }

    /**
     * 渲染一名佩戴者身上的全部可渲染跟随者：独立模式按抽取阶段算好的布局逐件渲染，编队模式由尾巴逐节渲染。
     */
    public static void renderAll(
            LivingEntityRenderState renderState,
            int packedLight,
            int outlineColor,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        FollowerRenderStateHolder renderStateHolder = (FollowerRenderStateHolder) renderState;
        List<FollowerState> followerStates = renderStateHolder.catburger$followerStates();
        if (followerStates == null || followerStates.isEmpty()) return;

        FollowerRenderData renderParams = renderStateHolder.catburger$renderParams();
        double displaySize = renderStateHolder.catburger$displaySize();
        float partialTicks = renderStateHolder.catburger$partialTick();
        if (renderParams.getRenderMode() == FollowerRenderMode.FORMATION) {
            renderFormation(
                    renderState,
                    renderStateHolder.catburger$formationState(),
                    followerStates,
                    renderParams,
                    displaySize,
                    packedLight,
                    outlineColor,
                    poseStack,
                    submitNodeCollector,
                    partialTicks
            );
            return;
        }

        double[] layout = renderStateHolder.catburger$layout();
        for (int index = 0; index < followerStates.size(); index++) {
            renderOne(
                    renderState,
                    followerStates.get(index),
                    layout[index * 3],
                    layout[index * 3 + 1],
                    layout[index * 3 + 2],
                    displaySize,
                    renderParams,
                    packedLight,
                    outlineColor,
                    poseStack,
                    submitNodeCollector,
                    partialTicks
            );
        }
    }

    /**
     * 编队模式：首件按自身状态渲染，其后各节取尾巴的世界坐标与朝向，缩放沿用首件。
     */
    private static void renderFormation(
            LivingEntityRenderState renderState,
            FollowerState followerState,
            List<FollowerState> followerStates,
            FollowerRenderData renderParams,
            double displaySize,
            int packedLight,
            int outlineColor,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            float partialTicks
    ) {
        followerState.updateFormation(
                renderState,
                renderParams,
                followerStates.size() - 1,
                renderParams.getFormationSpacing(),
                displaySize,
                partialTicks
        );
        float scale = followerState.scale();
        draw(
                followerStates.getFirst().itemRenderState(),
                followerState.xOffset(partialTicks),
                followerState.yOffset(partialTicks),
                followerState.zOffset(partialTicks),
                followerState.renderYaw(partialTicks),
                followerState.headPitch(partialTicks),
                followerState.gazeYaw(partialTicks),
                scale,
                packedLight,
                outlineColor,
                poseStack,
                submitNodeCollector
        );
        for (int index = 1; index < followerStates.size(); index++) {
            int segment = index - 1;
            draw(
                    followerStates.get(index).itemRenderState(),
                    followerState.tailXOffset(segment, partialTicks),
                    followerState.tailYOffset(segment, partialTicks),
                    followerState.tailZOffset(segment, partialTicks),
                    followerState.tailYaw(segment, partialTicks),
                    followerState.tailPitch(segment, partialTicks),
                    followerState.tailGazeYaw(segment, partialTicks),
                    scale,
                    packedLight,
                    outlineColor,
                    poseStack,
                    submitNodeCollector
            );
        }
    }

    /**
     * 取参与渲染的跟随者：跳过不可渲染的槽位，顺带按刻抽取物品渲染状态，
     * 开启去重时按“物品 + 解析出的模型身份”只保留顺序最靠前的一件。
     * 已知局限：模型身份只覆盖物品模型实例、附魔光效与染色值，资源重载后实例重建，跨重载不可比。
     */
    private static List<WornFollower> selectRendered(
            LivingEntity livingEntity,
            LivingEntityRenderState renderState,
            List<WornFollower> wornFollowers,
            FollowerRenderData renderParams,
            boolean preview
    ) {
        List<WornFollower> renderedFollowers = new ArrayList<>(wornFollowers.size());
        Map<Item, Set<Object>> renderedModels = new HashMap<>();
        for (WornFollower wornFollower : wornFollowers) {
            if (!wornFollower.renderable()) continue;
            FollowerState followerState = FollowerState.of(livingEntity, renderState, wornFollower.slotKey(), renderParams, preview);
            ItemStack itemStack = wornFollower.itemStack();
            followerState.extractItem(itemStack, renderParams, renderState);
            if (renderParams.isDeduplicateRendering()) {
                Item item = itemStack.getItem();
                Object modelIdentity = followerState.itemRenderState().getModelIdentity();
                if (!renderedModels.computeIfAbsent(item, key -> new HashSet<>()).add(modelIdentity)) continue;
            }

            renderedFollowers.add(wornFollower);
        }

        return renderedFollowers;
    }

    /**
     * 渲染单件跟随者：按槽位状态推进并取位置与朝向，预览与世界共用同一套推进。
     */
    private static void renderOne(
            LivingEntityRenderState renderState,
            FollowerState followerState,
            double layoutForward,
            double layoutRight,
            double layoutUp,
            double displaySize,
            FollowerRenderData renderParams,
            int packedLight,
            int outlineColor,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            float partialTicks
    ) {
        followerState.update(renderState, renderParams, layoutForward, layoutRight, layoutUp, displaySize, partialTicks);

        draw(
                followerState.itemRenderState(),
                followerState.xOffset(partialTicks),
                followerState.yOffset(partialTicks),
                followerState.zOffset(partialTicks),
                followerState.renderYaw(partialTicks),
                followerState.headPitch(partialTicks),
                followerState.gazeYaw(partialTicks),
                followerState.scale(),
                packedLight,
                outlineColor,
                poseStack,
                submitNodeCollector
        );
    }

    private static void draw(
            ItemStackRenderState itemRenderState,
            double xOffset,
            double yOffset,
            double zOffset,
            float renderYaw,
            float headPitch,
            float gazeYaw,
            float scale,
            int packedLight,
            int outlineColor,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        if (itemRenderState.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.scale(scale, scale, scale);
        // 几何中心不与模型原点重合，绕原点做俯仰会让整块几何划弧；先移到中心，转完再移回
        AABB modelBounds = itemRenderState.getModelBoundingBox();
        double modelCenterX = (modelBounds.minX + modelBounds.maxX) / 2.0;
        double modelCenterY = (modelBounds.minY + modelBounds.maxY) / 2.0;
        double modelCenterZ = (modelBounds.minZ + modelBounds.maxZ) / 2.0;
        poseStack.translate(modelCenterX, modelCenterY, modelCenterZ);
        // 模型正面朝北（-Z）建模，偏航按 (180 - 偏航) 换算，模型自身不再做任何翻转
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - renderYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-headPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-gazeYaw));
        poseStack.translate(-modelCenterX, -modelCenterY, -modelCenterZ);
        itemRenderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, outlineColor);
        poseStack.popPose();
    }
}
