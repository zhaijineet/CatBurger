package net.zhaiji.catburger.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.client.render.FollowerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(
            method = "renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD")
    )
    private static void catburger$renderEntityInInventoryFollowsAngle$beginPreview(
            GuiGraphicsExtractor guiGraphicsExtractor,
            int x0,
            int y0,
            int x1,
            int y1,
            int size,
            float offsetY,
            float xAngle,
            float yAngle,
            LivingEntity entity,
            CallbackInfo ci
    ) {
        FollowerRenderer.beginInventoryPreview(entity);
    }

    @Inject(
            method = "renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("RETURN")
    )
    private static void catburger$renderEntityInInventoryFollowsAngle$endPreview(
            GuiGraphicsExtractor guiGraphicsExtractor,
            int x0,
            int y0,
            int x1,
            int y1,
            int size,
            float offsetY,
            float xAngle,
            float yAngle,
            LivingEntity entity,
            CallbackInfo ci
    ) {
        FollowerRenderer.endInventoryPreview();
    }
}
