package net.zhaiji.catburger.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.item.BlubItem;
import net.zhaiji.catburger.item.CatBurgerItem;

public class CatBurgerItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CatBurger.MOD_ID);

    public static final RegistryObject<Item> CAT_BURGER = ITEMS.register(
            "cat_burger",
            CatBurgerItem::new
    );

    public static final RegistryObject<Item> BLUB = ITEMS.register(
            "blub",
            BlubItem::new
    );
}
