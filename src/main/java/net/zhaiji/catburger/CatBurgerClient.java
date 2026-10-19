package net.zhaiji.catburger;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.zhaiji.catburger.client.event.ClientEventManager;
import net.zhaiji.catburger.config.CatBurgerClientConfig;

public class CatBurgerClient {
    public static void init(IEventBus modBus, IEventBus gameBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CatBurgerClientConfig.SPEC);

        ClientEventManager.init(modBus, gameBus);
    }
}
