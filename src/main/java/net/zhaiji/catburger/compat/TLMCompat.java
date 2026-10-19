package net.zhaiji.catburger.compat;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.zhaiji.catburger.util.FollowerUtil;

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
        EntityMaid maid = event.getMaid();
        if (FollowerUtil.onDeathProtection(maid, maid.getCooldowns())) event.setCanceled(true);
    }
}
