package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.util.WornFollower;

public class FollowerRenderer {
    // 背包等界面预览期间为 true，由 InventoryScreenMixin 在 renderEntityInInventory 前后置位与复位
    private static boolean renderingInventoryPreview;

    public static void beginInventoryPreview() {
        renderingInventoryPreview = true;
    }

    public static void endInventoryPreview() {
        renderingInventoryPreview = false;
    }

    public static void render(
            LivingEntity livingEntity,
            WornFollower wornFollower,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        FollowerRenderData renderParams = RenderParamsResolver.resolve(livingEntity);
        double xOffset;
        double yOffset;
        double zOffset;
        float renderYaw;
        float headPitch;
        float localYaw;
        float scale;
        if (renderingInventoryPreview) {
            float viewYaw = livingEntity.getViewYRot(partialTicks);
            xOffset = FollowerState.previewXOffset(livingEntity, renderParams, viewYaw);
            yOffset = FollowerState.previewYOffset(livingEntity, renderParams, partialTicks);
            zOffset = FollowerState.previewZOffset(livingEntity, renderParams, viewYaw);
            renderYaw = viewYaw;
            headPitch = livingEntity.getViewXRot(partialTicks);
            localYaw = 0.0F;
            scale = FollowerState.previewScale(livingEntity, renderParams);
        } else {
            FollowerState followerState = FollowerState.of(livingEntity, wornFollower.slotKey(), renderParams);
            followerState.update(renderParams);
            xOffset = followerState.xOffset(partialTicks);
            yOffset = followerState.yOffset(partialTicks);
            zOffset = followerState.zOffset(partialTicks);
            renderYaw = followerState.renderYaw(partialTicks);
            headPitch = followerState.headPitch(partialTicks);
            localYaw = followerState.localYaw(partialTicks);
            scale = followerState.scale();
        }

        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.scale(scale, scale, scale);
        // 模型正面朝北（-Z）建模，偏航按 (180 - 偏航) 换算，模型自身不再做任何翻转
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - renderYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-headPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-localYaw));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                livingEntity,
                wornFollower.itemStack(),
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                livingEntity.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                livingEntity.getId()
        );
        poseStack.popPose();
    }
}
