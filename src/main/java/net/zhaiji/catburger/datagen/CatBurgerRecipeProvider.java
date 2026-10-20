package net.zhaiji.catburger.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.concurrent.CompletableFuture;

public class CatBurgerRecipeProvider extends RecipeProvider {
    protected CatBurgerRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // 每个 FollowerItem 一份图腾切石配方，配方名取物品注册名
        Ingredient totem = Ingredient.of(Items.TOTEM_OF_UNDYING);
        Criterion<?> hasTotem = has(Items.TOTEM_OF_UNDYING);
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .filter(deferredItem -> deferredItem.get() instanceof FollowerItem)
                            .forEach(deferredItem -> SingleItemRecipeBuilder.stonecutting(totem, RecipeCategory.MISC, deferredItem.get(), 1)
                                                                            .unlockedBy("has_totem", hasTotem)
                                                                            .save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CatBurger.MOD_ID, deferredItem.getId().getPath()))));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Items.BREAD), RecipeCategory.MISC, CatBurgerItems.FOLLOWER_CONFIG_CARD.get(), 1)
                                .unlockedBy("has_bread", has(Items.BREAD))
                                .save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CatBurger.MOD_ID, "follower_config_card")));
    }

    /**
     * 配方写入需要按注册表查表，因此由依赖注册表的Runner在真正生成时构造本类。
     */
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new CatBurgerRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "CatBurger Recipes";
        }
    }
}
