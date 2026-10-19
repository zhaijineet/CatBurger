package net.zhaiji.catburger.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.zhaiji.catburger.CatBurger;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

/**
 * 为玩家提供 Curios 的 head 槽，其他生物的饰品槽由给它们加槽的模组负责。
 */
public class CatBurgerCuriosDataProvider extends CuriosDataProvider {
    public CatBurgerCuriosDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(CatBurger.MOD_ID, output, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries) {
        createEntities("head")
                .addPlayer()
                .addSlots("head");
    }
}
