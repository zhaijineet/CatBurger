package net.zhaiji.catburger.datagen;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.function.Consumer;

public class CatBurgerRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public CatBurgerRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeWriter) {
        // 每个 FollowerItem 一份图腾切石配方，配方名取物品注册名
        Ingredient totem = Ingredient.of(Items.TOTEM_OF_UNDYING);
        InventoryChangeTrigger.TriggerInstance hasTotem = has(Items.TOTEM_OF_UNDYING);
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .filter(deferredItem -> deferredItem.get() instanceof FollowerItem)
                            .forEach(deferredItem -> SingleItemRecipeBuilder.stonecutting(totem, RecipeCategory.MISC, deferredItem.get())
                                                                            .unlockedBy("has_totem", hasTotem)
                                                                            .save(recipeWriter, ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, deferredItem.getId().getPath())));
    }
}
