package net.zhaiji.catburger.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenHandler {
    public static void handlerGatherDataEvent(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new CatBurgerRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new CatBurgerItemTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new CatBurgerCuriosDataProvider(packOutput, existingFileHelper, lookupProvider));

        generator.addProvider(event.includeClient(), new CatBurgerItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.EN_US));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.ZH_CN));
        generator.addProvider(event.includeClient(), new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.JA_JP));
    }
}
