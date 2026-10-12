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
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public final class CatBurgerRenderer {
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
            MultiBufferSource renderTypeBuffer,
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
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180)));
        poseStack.mulPose(Axis.YP.rotationDegrees(-transform.renderYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-transform.headPitch()));
        Minecraft.getInstance().getItemRenderer().render(
                itemStack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                renderTypeBuffer,
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
