package net.zhaiji.catburger.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.client.render.FollowerRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(
            method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD")
    )
    private static void catburger$renderEntityInInventory$beginPreview(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int scale,
            Quaternionf pose,
            @Nullable Quaternionf cameraOrientation,
            LivingEntity entity,
            CallbackInfo ci
    ) {
        FollowerRenderer.beginInventoryPreview();
        int[] clipOffsets = catburger$clipOffsetsOf(x, y, scale);
        if (clipOffsets == null) return;

        guiGraphics.enableScissor(x + clipOffsets[0], y + clipOffsets[1], x + clipOffsets[2], y + clipOffsets[3]);
    }

    @Inject(
            method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("RETURN")
    )
    private static void catburger$renderEntityInInventory$endPreview(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int scale,
            Quaternionf pose,
            @Nullable Quaternionf cameraOrientation,
            LivingEntity entity,
            CallbackInfo ci
    ) {
        if (catburger$clipOffsetsOf(x, y, scale) != null) guiGraphics.disableScissor();
        FollowerRenderer.endInventoryPreview();
    }

    /**
     * 按脚点相对容器界面的位置给出与原版1.21.1一致的裁剪框，偏移相对预览脚点；
     * 1.20.1没有原版裁剪，各界面把脚点放在不同位置，所以用脚点与缩放反推是哪个界面；
     * 不依赖具体界面类型，任意界面只要脚点与缩放匹配同一套标准布局就自动生效。
     */
    @Unique
    private static int[] catburger$clipOffsetsOf(int x, int y, int scale) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return null;

        int offsetX = x - containerScreen.getGuiLeft();
        int offsetY = y - containerScreen.getGuiTop();

        // 玩家背包与Curios装备栏：脚点leftPos+51、topPos+75，1.21.1的框是leftPos+26、topPos+8到leftPos+75、topPos+78
        if (scale == 30 && offsetX == 51 && offsetY == 75) return new int[]{-25, -67, 24, 3};
        // 创造模式物品栏：脚点leftPos+88、topPos+45，1.21.1的框是leftPos+73、topPos+6到leftPos+105、topPos+49
        if (scale == 20 && offsetX == 88 && offsetY == 45) return new int[]{-15, -39, 17, 4};
        // 马界面：脚点leftPos+51、topPos+60，1.21.1的框是leftPos+26、topPos+18到leftPos+78、topPos+70
        if (scale == 17 && offsetX == 51 && offsetY == 60) return new int[]{-25, -42, 27, 10};

        return null;
    }
}
