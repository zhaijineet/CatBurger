package net.zhaiji.catburger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.zhaiji.catburger.client.event.ClientEventManager;
import net.zhaiji.catburger.config.CatBurgerClientConfig;

@Mod(value = CatBurger.MOD_ID, dist = Dist.CLIENT)
public class CatBurgerClient {
    public CatBurgerClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CatBurgerClientConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ClientEventManager.init(modEventBus, NeoForge.EVENT_BUS);
    }
}
