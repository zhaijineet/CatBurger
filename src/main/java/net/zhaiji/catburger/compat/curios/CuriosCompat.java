package net.zhaiji.catburger.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.WornFollower;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Curios 侧的支持：给跟随者物品注册 Curio，并从 Curios 槽中发现已佩戴的跟随者。
 */
public class CuriosCompat {
    // Curios 槽位顺序不由其接口承诺，按槽位键排序才能得到稳定顺序，键中的索引已补零
    private static final Comparator<WornFollower> CURIOS_SLOT_ORDER = Comparator.comparing(WornFollower::slotKey);

    public static void init(IEventBus modBus, IEventBus gameBus) {
        CuriosCompat.modBusListener(modBus);
        CuriosCompat.gameBusListener(gameBus);
    }

    public static void modBusListener(IEventBus modBus) {
        modBus.addListener(CuriosCompat::handlerFMLCommonSetupEvent);
    }

    public static void gameBusListener(IEventBus gameBus) {
    }

    /**
     * 只给继承 {@link FollowerItem} 的物品注册 Curio，Curios 的物品级接口不接受空表，因此逐件注册。
     */
    public static void handlerFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<Item> registryObject : CatBurgerItems.ITEMS.getEntries()) {
                if (registryObject.get() instanceof FollowerItem followerItem)
                    CuriosApi.registerCurio(followerItem, new FollowerCurio());
            }
        });
    }

    /**
     * 由 Curios 槽标识与槽内索引构造槽位键，索引补零到 4 位，使键的字典序等于槽位的数值序。
     */
    private static String curiosSlotKey(String slotIdentifier, int slotIndex) {
        return String.format(Locale.ROOT, "curios:%s:%04d", slotIdentifier, slotIndex);
    }

    private static WornFollower toWornFollower(SlotResult slotResult) {
        SlotContext slotContext = slotResult.slotContext();
        return new WornFollower(slotResult.stack(), curiosSlotKey(slotContext.identifier(), slotContext.index()), slotContext.visible());
    }

    /**
     * 通过 Curios 查找全部生效槽位中的跟随者，结果按槽位键排序。
     */
    public static List<WornFollower> findAllInCurios(LivingEntity livingEntity) {
        // Curios 5.x 只有单参 findCurios，没有 1.21.1 那种带缓存键的重载，每次都会全量扫描槽位
        List<SlotResult> slotResults = CuriosApi.getCuriosInventory(livingEntity)
                                                .map(curiosItemHandler -> curiosItemHandler.findCurios(FollowerUtil::isFollower))
                                                .orElse(List.of());
        if (slotResults.isEmpty()) return List.of();

        // 排序需要可变列表，复制一份以免改动 Curios 返回的列表
        List<WornFollower> wornFollowers = new ArrayList<>();
        for (SlotResult slotResult : slotResults) wornFollowers.add(toWornFollower(slotResult));
        wornFollowers.sort(CURIOS_SLOT_ORDER);
        return wornFollowers;
    }
}
