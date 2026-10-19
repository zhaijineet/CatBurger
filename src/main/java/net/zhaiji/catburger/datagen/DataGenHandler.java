package net.zhaiji.catburger.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * 26.1 的服务端与客户端数据生成合并到单次 clientData 运行，与原版 NeoForge 自身的做法一致。
 */
public class DataGenHandler {
    public static void handlerGatherDataClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new CatBurgerRecipeProvider.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new CatBurgerItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new CatBurgerCuriosDataProvider(packOutput, lookupProvider));
        generator.addProvider(true, new CatBurgerItemModelProvider(packOutput));
        generator.addProvider(true, new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.EN_US));
        generator.addProvider(true, new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.ZH_CN));
        generator.addProvider(true, new CatBurgerLanguageProvider(packOutput, CatBurgerLanguageProvider.JA_JP));
    }
}
