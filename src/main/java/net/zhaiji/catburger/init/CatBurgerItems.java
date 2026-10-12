package net.zhaiji.catburger.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.item.CatBurgerItem;

public class CatBurgerItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CatBurger.MOD_ID);

    public static final DeferredItem<Item> CAT_BURGER = ITEMS.register(
            "cat_burger",
            CatBurgerItem::new
    );
}
