package net.zhaiji.catburger.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.client.render.CatBurgerFollowState.CatBurgerFollowTransform;

@OnlyIn(Dist.CLIENT)
public final class CatBurgerRenderer {
    // 背包等界面预览期间为 true，由 InventoryScreenMixin 在 renderEntityInInventory 前后置位与复位
    private static boolean renderingInventoryPreview;

    private CatBurgerRenderer() {
    }

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
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(-transform.renderYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-transform.headPitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(transform.roll()));
        Minecraft.getInstance().getItemRenderer().render(
                itemStack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                getModel()
        );
        poseStack.popPose();
    }

    private static BakedModel getModel() {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "cat_burger")));
    }
}
