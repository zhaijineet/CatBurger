package net.zhaiji.catburger.equipment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zhaiji.catburger.compat.CompatManager;
import net.zhaiji.catburger.compat.CuriosCompat;
import net.zhaiji.catburger.init.CatBurgerItems;

import java.util.Optional;

public final class CatBurgerEquipment {
    public record WornCatBurger(ItemStack itemStack, boolean renderable) {
    }

    private CatBurgerEquipment() {
    }

    /**
     * 按原版头盔槽优先、Curios 槽位其次的顺序定位已佩戴的猫猫汉堡。
     */
    public static Optional<WornCatBurger> find(LivingEntity livingEntity) {
        ItemStack headStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (headStack.is(CatBurgerItems.CAT_BURGER.get())) {
            return Optional.of(new WornCatBurger(headStack, true));
        }

        if (CompatManager.CURIOS_LOADED) {
            return CuriosCompat.findInCurios(livingEntity);
        }

        return Optional.empty();
    }
}
