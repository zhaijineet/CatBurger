package net.zhaiji.catburger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.event.CommonEventManager;
import net.zhaiji.catburger.init.CatBurgerAttachmentTypes;
import net.zhaiji.catburger.init.CatBurgerCreativeModeTabs;
import net.zhaiji.catburger.init.CatBurgerDataComponents;
import net.zhaiji.catburger.init.CatBurgerItems;

@Mod(CatBurger.MOD_ID)
public class CatBurger {
    public static final String MOD_ID = "catburger";

    public CatBurger(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, CatBurgerServerConfig.SPEC);

        CatBurgerItems.ITEMS.register(modEventBus);
        CatBurgerCreativeModeTabs.CREATIVE_MODE_TAB.register(modEventBus);
        CatBurgerAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        CatBurgerDataComponents.COMPONENT_TYPES.register(modEventBus);

        CommonEventManager.init(modEventBus, NeoForge.EVENT_BUS);
    }
}
