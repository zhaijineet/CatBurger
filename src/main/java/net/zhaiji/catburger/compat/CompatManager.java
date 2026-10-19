package net.zhaiji.catburger.compat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.LoadingModList;
import net.zhaiji.catburger.compat.curios.CuriosCompat;

public class CompatManager {
    public static final boolean TLM_LOADED = LoadingModList.get().getModFileById("touhou_little_maid") != null;
    public static final boolean CURIOS_LOADED = LoadingModList.get().getModFileById("curios") != null;

    public static void init(IEventBus modBus, IEventBus gameBus) {
        if (TLM_LOADED) TLMCompat.init(modBus, gameBus);
        if (CURIOS_LOADED) CuriosCompat.init(modBus, gameBus);
    }
}
