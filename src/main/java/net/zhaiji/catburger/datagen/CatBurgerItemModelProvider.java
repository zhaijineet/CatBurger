package net.zhaiji.catburger.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

/**
 * 为每个 FollowerItem 生成物品模型：几何体取自 item/follower 下的原始模型，display 在此按新模型朝向声明，两者用 composite 合并成一个模型。
 */
public class CatBurgerItemModelProvider extends ItemModelProvider {
    public CatBurgerItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CatBurger.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .filter(deferredItem -> deferredItem.get() instanceof FollowerItem)
                            .forEach(deferredItem -> {
                                String path = deferredItem.getId().getPath();
                                ResourceLocation geometry = modLoc("item/follower/" + path);
                                getBuilder(path)
                                        .parent(getExistingFile(mcLoc("block/block")))
                                        .customLoader(CompositeModelBuilder::begin)
                                        .child("geometry", new ItemModelBuilder(geometry, existingFileHelper).parent(getExistingFile(geometry)))
                                        .end()
                                        .transforms()
//                                        .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
//                                        .scale(0.375F)
//                                        .end()
                                        .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                                        .rotation(0F, 142F, 0F)
                                        .translation(2F, 3.5F, 0F)
                                        .scale(0.4F)
                                        .end()
                                        .end();
                            });
    }
}
