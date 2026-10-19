package net.zhaiji.catburger.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.zhaiji.catburger.compat.curios.CuriosCompat;

public class CompatManager {
    public static final boolean TLM_LOADED = ModList.get().isLoaded("touhou_little_maid");
    public static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    public static void init(IEventBus modBus, IEventBus gameBus) {
        if (TLM_LOADED) TLMCompat.init(modBus, gameBus);
        if (CURIOS_LOADED) CuriosCompat.init(modBus, gameBus);
    }
}
