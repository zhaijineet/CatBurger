package net.zhaiji.catburger.compat;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.neoforged.bus.api.IEventBus;
import net.zhaiji.catburger.config.CatBurgerCommonConfig;
import net.zhaiji.catburger.equipment.CatBurgerEquipment;
import net.zhaiji.catburger.init.InitItem;

public class TLMCompat {
    public static void init(IEventBus modBus, IEventBus gameBus) {
        TLMCompat.modBusListener(modBus);
        TLMCompat.gameBusListener(gameBus);
    }

    public static void modBusListener(IEventBus modBus) {
    }

    public static void gameBusListener(IEventBus gameBus) {
        gameBus.addListener(TLMCompat::handlerMaidDeathEvent);
    }

    public static void handlerMaidDeathEvent(MaidDeathEvent event) {
        if (!CatBurgerCommonConfig.DEATH_PROTECTION_ENABLED.get()) return;
        EntityMaid maid = event.getMaid();
        Item item = InitItem.CAT_BURGER.get();
        ItemCooldowns cooldowns = maid.getCooldowns();
        if (CatBurgerEquipment.find(maid).isEmpty() || cooldowns.isOnCooldown(item)) {
            return;
        }

        if (CatBurgerCommonConfig.DEATH_PROTECTION_USE_HEALTH_PERCENTAGE.get()) {
            float maxHealth = maid.getMaxHealth();
            float restoreAmount = maxHealth * (CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_HEALTH_PERCENTAGE.get().floatValue() / 100.0f);
            maid.setHealth(restoreAmount);
        } else {
            maid.setHealth(CatBurgerCommonConfig.DEATH_PROTECTION_RESTORE_HEALTH.get());
        }
        cooldowns.addCooldown(item, CatBurgerCommonConfig.DEATH_PROTECTION_COOLDOWN.get());
        maid.level().broadcastEntityEvent(maid, (byte) 35);
        event.setCanceled(true);
    }
}
