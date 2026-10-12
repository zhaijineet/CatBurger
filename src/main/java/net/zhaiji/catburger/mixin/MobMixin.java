package net.zhaiji.catburger.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.config.CatBurgerCommonConfig;
import net.zhaiji.catburger.init.InitItem;
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
    public void catBurger$canHoldItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!CatBurgerCommonConfig.MOB_PICKUP_ENABLED.get() && stack.is(InitItem.CAT_BURGER.get())) {
            cir.setReturnValue(false);
        }
    }
}
