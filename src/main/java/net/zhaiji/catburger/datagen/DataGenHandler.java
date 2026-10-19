package net.zhaiji.catburger.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGenHandler {
    public static void handlerGatherDataEvent(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // 1.20.1 的 RecipeProvider 只接受 PackOutput，不需要 lookupProvider
        generator.addProvider(event.includeServer(), new CatBurgerRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new CatBurgerItemTagsProvider(packOutput, event.getLookupProvider(), existingFileHelper));
        generator.addProvider(event.includeServer(), new CatBurgerCuriosDataProvider(packOutput, existingFileHelper, event.getLookupProvider()));

        generator.addProvider(event.includeClient(), new CatBurgerItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.EN_US));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.ZH_CN));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.JA_JP));
    }
}
