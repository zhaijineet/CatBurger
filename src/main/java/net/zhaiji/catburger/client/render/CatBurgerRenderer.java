package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zhaiji.catburger.client.render.CatBurgerFollowState.CatBurgerFollowTransform;

@OnlyIn(Dist.CLIENT)
public class CatBurgerRenderer {
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
            ItemStack itemStack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    ) {
        CatBurgerFollowTransform transform;
        if (renderingInventoryPreview) {
            transform = CatBurgerFollowState.previewTransform(livingEntity, partialTicks);
        } else {
            CatBurgerFollowState followState = CatBurgerFollowState.of(livingEntity);
            followState.update(livingEntity);
            transform = followState.resolve(livingEntity, partialTicks);
        }

        poseStack.pushPose();
        poseStack.translate(transform.xOffset(), transform.yOffset(), transform.zOffset());
        float scale = transform.scale();
        poseStack.scale(scale, scale, scale);
        // 模型正面朝北（-Z）建模，偏航按 (180 - 偏航) 换算，模型自身不再做任何翻转
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - transform.renderYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-transform.headPitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(transform.roll()));
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
                livingEntity.getId()
        );
        poseStack.popPose();
    }
}
