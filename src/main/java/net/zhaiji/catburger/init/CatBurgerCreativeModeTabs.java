package net.zhaiji.catburger.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.catburger.CatBurger;

import java.util.function.Supplier;

public class CatBurgerCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CatBurger.MOD_ID);

    public static final String CATBURGER_TAB_TRANSLATABLE = "creativetab.catburger.catburger_tab";

    public static final Supplier<CreativeModeTab> CATBURGER_TAB = CREATIVE_MODE_TAB.register(
            "catburger_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(CatBurgerItems.CAT_BURGER.get()))
                    .title(Component.translatable(CATBURGER_TAB_TRANSLATABLE))
                    .displayItems(((itemDisplayParameters, output) -> {
                        CatBurgerItems.ITEMS.getEntries()
                                            .stream()
                                            .map(Supplier::get)
                                            .forEach(output::accept);
                    }))
                    .build()
    );
}