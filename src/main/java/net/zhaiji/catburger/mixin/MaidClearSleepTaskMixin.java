// TLM（Touhou Little Maid）在 26.1.2 没有发布版本，包路径不存在，暂时整体停用，本文件当前不产生任何类（用户决策：保留代码不删除）。
// 停用期间 catburger.mixins.json 的 mixins 数组也移除了 MaidClearSleepTaskMixin 这一项，恢复时需一并加回。
// 恢复条件：TLM 发布 26.1.2 版本后取消下方注释，并从 neoforge_1.21.1 分支取回同名实现做对照；
// 恢复时需重新核对 MaidClearSleepTask#start 的参数表与 EntityMaid#stopSleeping 的调用点是否仍在。

//package net.zhaiji.catburger.mixin;
//
//import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidClearSleepTask;
//import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
//import net.minecraft.server.level.ServerLevel;
//import net.zhaiji.catburger.util.FollowerUtil;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Pseudo;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Pseudo
//@Mixin(MaidClearSleepTask.class)
//public abstract class MaidClearSleepTaskMixin {
//    /**
//     * 如果睡眠被打断似乎也会触发？那这件事可万万不可告诉玩家
//     */
//    @Inject(
//            method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;stopSleeping()V"
//            )
//    )
//    public void catburger$start(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn, CallbackInfo ci) {
//        FollowerUtil.onWakeUp(entityIn, entityIn.getCooldowns());
//    }
//}
