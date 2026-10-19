package net.zhaiji.catburger.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 为每个 FollowerItem 生成物品模型：几何体取自 item/follower 下的原始模型，display 在此按新模型朝向声明，两者用 composite 合并成一个模型。
 */
public class CatBurgerItemModelProvider extends ModelProvider {
    public CatBurgerItemModelProvider(PackOutput output) {
        super(output, CatBurger.MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return CatBurgerItems.ITEMS.getEntries().stream();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        CatBurgerItems.ITEMS.getEntries()
                            .stream()
                            .map(Supplier::get)
                            .forEach(item -> {
                                if (item instanceof FollowerItem) registerFollowerModel(item, itemModels);
                                else itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
                            });
    }

    private void registerFollowerModel(Item item, ItemModelGenerators itemModels) {
        String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();
        Identifier modelId = modLocation("item/" + itemPath);

        itemModels.modelOutput.accept(modelId, () -> buildCompositeModel(modLocation("item/follower/" + itemPath)));
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelId));
    }

    /**
     * 外层模型只做两件事：把几何体挂成唯一子模型，并声明第一人称的摆放；朝向由新模型决定，因此必须在这里覆盖。
     */
    private JsonObject buildCompositeModel(Identifier geometryId) {
        JsonObject geometryReference = new JsonObject();
        geometryReference.addProperty("parent", geometryId.toString());

        JsonObject children = new JsonObject();
        children.add("geometry", geometryReference);

        JsonObject firstPersonRightHand = new JsonObject();
        firstPersonRightHand.add("rotation", numbers(0F, 142F, 0F));
        firstPersonRightHand.add("translation", numbers(2F, 3.5F, 0F));
        firstPersonRightHand.add("scale", numbers(0.4F, 0.4F, 0.4F));

        JsonObject display = new JsonObject();
        display.add("firstperson_righthand", firstPersonRightHand);

        JsonObject model = new JsonObject();
        model.addProperty("parent", mcLocation("block/block").toString());
        model.addProperty("loader", Identifier.fromNamespaceAndPath("neoforge", "composite").toString());
        model.add("children", children);
        model.add("display", display);
        return model;
    }

    private static JsonArray numbers(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) array.add(new JsonPrimitive(value));
        return array;
    }
}
