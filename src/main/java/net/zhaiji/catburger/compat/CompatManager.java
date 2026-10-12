package net.zhaiji.catburger.compat;

import net.neoforged.fml.ModList;

public class CompatManager {
    public static final boolean TLM_LOADED = ModList.get().isLoaded("touhou_little_maid");
    public static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
}
