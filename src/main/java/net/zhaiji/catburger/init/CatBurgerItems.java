package net.zhaiji.catburger.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.item.BlubItem;
import net.zhaiji.catburger.item.CatBurgerItem;
import net.zhaiji.catburger.item.FollowerConfigCardItem;

public class CatBurgerItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CatBurger.MOD_ID);

    public static final DeferredItem<Item> FOLLOWER_CONFIG_CARD = ITEMS.registerItem(
            "follower_config_card",
            FollowerConfigCardItem::new
    );

    public static final DeferredItem<Item> CAT_BURGER = ITEMS.registerItem(
            "cat_burger",
            CatBurgerItem::new
    );

    public static final DeferredItem<Item> BLUB = ITEMS.registerItem(
            "blub",
            BlubItem::new
    );
}
