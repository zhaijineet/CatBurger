package net.zhaiji.catburger.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.concurrent.CompletableFuture;

public class CatBurgerRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public CatBurgerRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 每个 FollowerItem 一份图腾切石配方，配方名取物品注册名
        Ingredient totem = Ingredient.of(Items.TOTEM_OF_UNDYING);
        Criterion<?> hasTotem = has(Items.TOTEM_OF_UNDYING);
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .filter(deferredItem -> deferredItem.get() instanceof FollowerItem)
                            .forEach(deferredItem -> SingleItemRecipeBuilder.stonecutting(totem, RecipeCategory.MISC, deferredItem.get())
                                                                            .unlockedBy("has_totem", hasTotem)
                                                                            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, deferredItem.getId().getPath())));
    }
}
