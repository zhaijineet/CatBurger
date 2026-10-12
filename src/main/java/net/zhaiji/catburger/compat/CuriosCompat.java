package net.zhaiji.catburger.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zhaiji.catburger.equipment.CatBurgerEquipment.WornCatBurger;
import net.zhaiji.catburger.init.InitItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Optional;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(CuriosCompat::registerCapabilities);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (itemStack, context) -> new CatBurgerCurio(itemStack), InitItem.CAT_BURGER);
    }

    /**
     * 通过 Curios 查找首个生效槽位中的猫猫汉堡及其槽位渲染开关。
     */
    public static Optional<WornCatBurger> findInCurios(LivingEntity livingEntity) {
        return CuriosApi.getCuriosInventory(livingEntity)
                .flatMap(curiosItemHandler -> curiosItemHandler.findFirstCurio(InitItem.CAT_BURGER.get()))
                .map(slotResult -> new WornCatBurger(slotResult.stack(), slotResult.slotContext().visible()));
    }

    private static final class CatBurgerCurio implements ICurio {
        private final ItemStack itemStack;

        private CatBurgerCurio(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public ItemStack getStack() {
            return this.itemStack;
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext) {
            return true;
        }
    }
}
