package net.zhaiji.catburger.event;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.zhaiji.catburger.compat.CompatManager;
import net.zhaiji.catburger.compat.TLMCompat;
import net.zhaiji.catburger.compat.curios.CuriosCompat;
import net.zhaiji.catburger.datagen.DataGenHandler;
import net.zhaiji.catburger.network.PacketManager;

public class CommonEventManager {
    public static void init(IEventBus modBus, IEventBus gameBus) {
        CommonEventManager.modBusListener(modBus);
        CommonEventManager.gameBusListener(gameBus);
        if (CompatManager.TLM_LOADED) TLMCompat.init(modBus, gameBus);
        if (CompatManager.CURIOS_LOADED) CuriosCompat.init(modBus, gameBus);
    }

    public static void modBusListener(IEventBus modBus) {
        modBus.addListener(DataGenHandler::handlerGatherDataEvent);
        modBus.addListener(PacketManager::handlerRegisterPayloadHandlersEvent);
    }

    public static void gameBusListener(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGHEST, CommonEventHandler::handlerLivingDeathEvent);
        gameBus.addListener(CommonEventHandler::handlerPlayerStartTrackingEvent);
        gameBus.addListener(CommonEventHandler::handlerPlayerWakeUpEvent);
        gameBus.addListener(CommonEventHandler::handlerEntityTickEvent$Post);
    }
}
