package net.zhaiji.catburger.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CatBurgerItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
    // Curios 的 head 槽靠该标签判定物品能否放入
    private static final TagKey<Item> CURIOS_HEAD = ItemTags.create(Identifier.fromNamespaceAndPath("curios", "head"));

    public CatBurgerItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ITEM, lookupProvider, item -> BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(), CatBurger.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        TagAppender<Item, Item> curiosHead = tag(CURIOS_HEAD);
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .map(Supplier::get)
                            .filter(item -> item instanceof FollowerItem)
                            .forEach(curiosHead::add);
    }
}
