package net.zhaiji.catburger.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.item.FollowerItem;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.WornFollower;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Curios 侧的支持：给跟随者物品注册能力，并从 Curios 槽中发现已佩戴的跟随者。
 */
public class CuriosCompat {
    // 非空缓存键让 Curios 按 gameTime 缓存查询结果，同一 tick 内的派发与渲染共用一次扫描
    private static final String FOLLOWER_CACHE_KEY = CatBurger.MOD_ID + ":followers";

    // Curios 槽位顺序不由其接口承诺，按槽位键排序才能得到稳定顺序，键中的索引已补零
    private static final Comparator<WornFollower> CURIOS_SLOT_ORDER = Comparator.comparing(WornFollower::slotKey);

    public static void init(IEventBus modBus, IEventBus gameBus) {
        CuriosCompat.modBusListener(modBus);
        CuriosCompat.gameBusListener(gameBus);
    }

    public static void modBusListener(IEventBus modBus) {
        modBus.addListener(CuriosCompat::registerCapabilities);
    }

    public static void gameBusListener(IEventBus gameBus) {
    }

    /**
     * 只给继承 {@link FollowerItem} 的物品注册 Curios 能力，物品表为空时跳过注册，因为注册接口不接受空表。
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Item[] followerItems = CatBurgerItems.ITEMS.getEntries()
                                                   .stream()
                                                   .map(Supplier::get)
                                                   .filter(item -> item instanceof FollowerItem)
                                                   .toArray(Item[]::new);
        if (followerItems.length == 0) return;

        event.registerItem(
                CuriosCapability.ITEM,
                (itemStack, context) -> new FollowerCurio(itemStack),
                followerItems
        );
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
        List<SlotResult> slotResults = CuriosApi.getCuriosInventory(livingEntity)
                                                .map(curiosItemHandler -> curiosItemHandler.findCurios(FollowerUtil::isFollower, false, FOLLOWER_CACHE_KEY))
                                                .orElse(List.of());
        if (slotResults.isEmpty()) return List.of();

        // 排序需要可变列表，复制一份以免改动 Curios 返回的同刻缓存
        List<WornFollower> wornFollowers = new ArrayList<>();
        for (SlotResult slotResult : slotResults) wornFollowers.add(toWornFollower(slotResult));
        wornFollowers.sort(CURIOS_SLOT_ORDER);
        return wornFollowers;
    }
}
