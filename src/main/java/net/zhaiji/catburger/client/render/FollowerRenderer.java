package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.util.WornFollower;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FollowerRenderer {
    private static final double MIN_DISPLAY_SIZE = 0.1;
    // 物品模型的几何中心，按物品缓存并记下算中心用的模型实例：实例变化时重算，动态模型也不会撑大缓存；
    // 同一物品经 ItemOverrides 解析出多个模型时会互相覆盖、退化为各自重算，正确性不受影响
    private static final Map<Item, ModelCenter> MODEL_CENTERS = new HashMap<>();

    // 背包等界面预览期间为 true，由 InventoryScreenMixin 在 renderEntityInInventory 前后置位与复位；
    // 它只决定状态取哪一份、partialTicks怎么取、是否消费位移与解析准心，跟随逻辑本身是同一套
    private static boolean renderingInventoryPreview;

    public static void beginInventoryPreview() {
        renderingInventoryPreview = true;
    }

    public static void endInventoryPreview() {
        renderingInventoryPreview = false;
    }

    /**
     * 渲染一名佩戴者身上的全部可渲染跟随者：去重后独立模式求一份散布布局逐件渲染，编队模式由尾巴逐节渲染。
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

        // 原版预览传入的partialTicks恒为1.0，直接沿用会让状态按刻跳变，改取与世界渲染相同的帧间比例
        if (renderingInventoryPreview) {
            partialTicks = Minecraft.getInstance().getFrameTime();
        }

        if (renderParams.getRenderMode() == FollowerRenderMode.FORMATION) {
            renderFormation(livingEntity, renderedFollowers, renderParams, displaySize, poseStack, buffer, packedLight, partialTicks);
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
        for (int index = 0; index < renderedFollowers.size(); index++) {
            renderOne(
                    livingEntity,
                    renderedFollowers.get(index),
                    layout[index * 3],
                    layout[index * 3 + 1],
                    layout[index * 3 + 2],
                    displaySize,
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
        FollowerState followerState = FollowerState.of(livingEntity, FollowerState.FORMATION_KEY, renderParams, renderingInventoryPreview);
        followerState.updateFormation(
                renderParams,
                renderedFollowers.size() - 1,
                renderParams.getFormationSpacing(),
                displaySize,
                partialTicks,
                renderingInventoryPreview
        );
        float scale = followerState.scale();
        draw(
                livingEntity,
                renderedFollowers.get(0).itemStack(),
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
        Map<Item, Set<BakedModel>> renderedModels = new HashMap<>();
        for (WornFollower wornFollower : wornFollowers) {
            if (!wornFollower.renderable()) continue;
            if (renderParams.isDeduplicateRendering()) {
                ItemStack itemStack = wornFollower.itemStack();
                BakedModel model = Minecraft.getInstance().getItemRenderer()
                        .getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId() + ItemDisplayContext.HEAD.ordinal());
                Item item = itemStack.getItem();
                if (!renderedModels.computeIfAbsent(item, key -> new HashSet<>()).add(model)) continue;
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
                .getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId() + ItemDisplayContext.HEAD.ordinal());
        PoseStack transformStack = new PoseStack();
        model.applyTransform(ItemDisplayContext.HEAD, transformStack, false);
        Vector3f headScale = transformStack.last().pose().getScale(new Vector3f());
        float modelScale = Math.max(headScale.x(), Math.max(headScale.y(), headScale.z()));
        return Math.max(modelScale * FollowerState.renderScale(livingEntity, renderParams), MIN_DISPLAY_SIZE);
    }

    /**
     * 渲染单件跟随者：按槽位状态推进并取位置与朝向，预览与世界共用同一套推进。
     */
    private static void renderOne(
            LivingEntity livingEntity,
            WornFollower wornFollower,
            double layoutForward,
            double layoutRight,
            double layoutUp,
            double displaySize,
            FollowerRenderData renderParams,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        FollowerState followerState = FollowerState.of(livingEntity, wornFollower.slotKey(), renderParams, renderingInventoryPreview);
        followerState.update(renderParams, layoutForward, layoutRight, layoutUp, displaySize, partialTicks, renderingInventoryPreview);

        draw(
                livingEntity,
                wornFollower.itemStack(),
                followerState.xOffset(partialTicks),
                followerState.yOffset(partialTicks),
                followerState.zOffset(partialTicks),
                followerState.renderYaw(partialTicks),
                followerState.headPitch(partialTicks),
                followerState.gazeYaw(partialTicks),
                followerState.scale(),
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
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        // seed 与 CustomHeadLayer 一致：entityId + 头部显示上下文的序号
        BakedModel model = itemRenderer.getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId() + ItemDisplayContext.HEAD.ordinal());
        // 几何中心不与模型原点重合，绕原点做俯仰会让整块几何划弧；先移到中心，转完再移回
        Vec3 center = modelCenterOf(itemStack.getItem(), model);
        poseStack.translate(center.x, center.y, center.z);
        // 模型正面朝北（-Z）建模，偏航按 (180 - 偏航) 换算，模型自身不再做任何翻转
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - renderYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-headPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-gazeYaw));
        poseStack.translate(-center.x, -center.y, -center.z);
        itemRenderer.render(
                itemStack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                model
        );
        poseStack.popPose();
    }

    /**
     * 取物品模型几何中心在渲染坐标系中的位置，按物品缓存：记录里的模型实例与当前一致时直接返回，否则重算。
     */
    private static Vec3 modelCenterOf(Item item, BakedModel model) {
        ModelCenter cached = MODEL_CENTERS.get(item);
        if (cached != null && cached.model() == model) {
            return cached.center();
        }

        Vec3 center = computeModelCenter(model);
        MODEL_CENTERS.put(item, new ModelCenter(model, center));
        return center;
    }

    /**
     * 遍历烘焙模型的全部顶点求包围盒，并换算到渲染坐标系：render 内部先施加头部显示变换、再整体平移 -0.5，
     * 顶点落到调用者空间时是「先减半格、再乘变换」，中心照此换算。
     * 这里只统计不带剔除面的四边形：全部面带剔除的方块模型与自定义渲染器物品会退回零中心，
     * 前者对单位立方块恰好正确，后者等于不做中心修正；现有跟随者物品模型都是单层无剔除面。
     */
    private static Vec3 computeModelCenter(BakedModel model) {
        AABB bounds = null;
        // 1.20.1 只有三参形式，不需要按渲染类型过滤
        for (BakedQuad quad : model.getQuads(null, null, RandomSource.create(42))) {
            int[] vertices = quad.getVertices();
            // 每顶点 8 个 int，前 3 个为 x/y/z，与 ModelBlockRenderer.calculateShape 的读法一致
            for (int index = 0; index < vertices.length; index += 8) {
                double x = Float.intBitsToFloat(vertices[index]);
                double y = Float.intBitsToFloat(vertices[index + 1]);
                double z = Float.intBitsToFloat(vertices[index + 2]);
                AABB vertex = new AABB(x, y, z, x, y, z);
                bounds = bounds == null ? vertex : bounds.minmax(vertex);
            }
        }
        if (bounds == null) return Vec3.ZERO;

        Vec3 center = bounds.getCenter();
        PoseStack transformStack = new PoseStack();
        model.applyTransform(ItemDisplayContext.HEAD, transformStack, false);
        Vector3f centerInCallerSpace = new Vector3f(
                (float) center.x - 0.5F,
                (float) center.y - 0.5F,
                (float) center.z - 0.5F
        );
        centerInCallerSpace.mulPosition(transformStack.last().pose());
        return new Vec3(centerInCallerSpace.x, centerInCallerSpace.y, centerInCallerSpace.z);
    }

    /**
     * 资源重载后模型实例必然重建，条目会按实例比对自行重算；这里主动清空只为及时释放旧模型引用。
     */
    public static void invalidateModelCenters() {
        MODEL_CENTERS.clear();
    }
}
