// TLM（Touhou Little Maid）在 26.1.2 没有发布版本，包路径不存在，暂时整体停用，本文件当前不产生任何类（用户决策：保留代码不删除）。
// 恢复条件：TLM 发布 26.1.2 版本后取消下方注释，并从 neoforge_1.21.1 分支取回同名实现做对照；
// 恢复时需重新核对 MaidDeathEvent#getMaid、EntityMaid#getCooldowns 是否仍为这两个名字与签名。

//package net.zhaiji.catburger.compat;
//
//import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
//import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
//import net.neoforged.bus.api.IEventBus;
//import net.zhaiji.catburger.util.FollowerUtil;
//
//public class TLMCompat {
//    public static void init(IEventBus modBus, IEventBus gameBus) {
//        TLMCompat.modBusListener(modBus);
//        TLMCompat.gameBusListener(gameBus);
//    }
//
//    public static void modBusListener(IEventBus modBus) {
//    }
//
//    public static void gameBusListener(IEventBus gameBus) {
//        gameBus.addListener(TLMCompat::handlerMaidDeathEvent);
//    }
//
//    public static void handlerMaidDeathEvent(MaidDeathEvent event) {
//        EntityMaid maid = event.getMaid();
//        if (FollowerUtil.onDeathProtection(maid, maid.getCooldowns())) event.setCanceled(true);
//    }
//}
