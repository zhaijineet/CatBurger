package net.zhaiji.catburger.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.zhaiji.catburger.client.render.FollowerState;
import net.zhaiji.catburger.client.render.FollowerStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 剔除包围盒并入跟随者占位，跟随者在视野内时佩戴者不被剔除。
 * 剔除盒钩子自 1.21.9 起由实体移到了渲染器，因此这里按渲染器注入。
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(
            method = "getBoundingBoxForCulling(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/AABB;",
            at = @At("RETURN"),
            cancellable = true
    )
    public void catburger$getBoundingBoxForCulling(Entity entity, CallbackInfoReturnable<AABB> cir) {
        if (!(entity instanceof FollowerStateHolder followerStateHolder)) return;

        Map<String, FollowerState> followerStates = followerStateHolder.catburger$followerStates();
        if (followerStates.isEmpty()) return;

        AABB cullingBox = cir.getReturnValue();
        for (FollowerState followerState : followerStates.values()) {
            cullingBox = cullingBox.minmax(followerState.cullingBox());
        }
        cir.setReturnValue(cullingBox);
    }
}
