package net.zhaiji.catburger.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.util.FollowerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(
            method = "canHoldItem(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void catburger$canHoldItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!CatBurgerServerConfig.MOB_PICKUP_ENABLED.get() && FollowerUtil.isFollower(stack)) cir.setReturnValue(false);
    }
}
