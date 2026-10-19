package net.zhaiji.catburger.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.zhaiji.catburger.compat.curios.CuriosCompat;

public class CompatManager {
    // TLM 在 26.1.2 没有发布版本，暂时停用；恢复时取消注释，并恢复 init 里对 TLMCompat 的接入
    // public static final boolean TLM_LOADED = ModList.get().isLoaded("touhou_little_maid");
    public static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    public static void init(IEventBus modBus, IEventBus gameBus) {
        if (CURIOS_LOADED) CuriosCompat.init(modBus, gameBus);
        // if (TLM_LOADED) TLMCompat.init(modBus, gameBus);
    }
}
