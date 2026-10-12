package net.zhaiji.catburger.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;

import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {
    public RecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Items.TOTEM_OF_UNDYING), RecipeCategory.MISC, CatBurgerItems.CAT_BURGER.get())
                .unlockedBy("has_totem", has(Items.TOTEM_OF_UNDYING))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "cat_burger"));
    }
}
