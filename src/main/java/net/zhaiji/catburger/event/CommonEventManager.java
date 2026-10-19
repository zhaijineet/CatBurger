package net.zhaiji.catburger.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.zhaiji.catburger.compat.CompatManager;
import net.zhaiji.catburger.datagen.DataGenHandler;

public class CommonEventManager {
    public static void init(IEventBus modBus, IEventBus gameBus) {
        CommonEventManager.modBusListener(modBus);
        CommonEventManager.gameBusListener(gameBus);
        CompatManager.init(modBus, gameBus);
    }

    public static void modBusListener(IEventBus modBus) {
        modBus.addListener(DataGenHandler::handlerGatherDataEvent);
    }

    public static void gameBusListener(IEventBus gameBus) {
        // AttachCapabilitiesEvent 是泛型事件，Forge 只接受 addGenericListener
        gameBus.addGenericListener(Entity.class, CommonEventHandler::handlerAttachCapabilitiesEvent);
        gameBus.addListener(EventPriority.HIGHEST, CommonEventHandler::handlerLivingDeathEvent);
        gameBus.addListener(CommonEventHandler::handlerPlayerStartTrackingEvent);
        gameBus.addListener(CommonEventHandler::handlerPlayerEvent$Clone);
        gameBus.addListener(CommonEventHandler::handlerPlayerWakeUpEvent);
        gameBus.addListener(CommonEventHandler::handlerLivingEvent$LivingTickEvent);
    }
}
