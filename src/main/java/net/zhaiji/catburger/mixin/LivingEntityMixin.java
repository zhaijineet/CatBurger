package net.zhaiji.catburger.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.client.render.FollowerState;
import net.zhaiji.catburger.client.render.FollowerStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * 让每个实体自带一份按槽位分列的跟随者状态，状态随实体生灭，不需要全局状态表与手动回收。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements FollowerStateHolder {
    @Unique
    private final Map<String, FollowerState> catburger$followerStates = new HashMap<>();

    @Override
    public Map<String, FollowerState> catburger$followerStates() {
        return catburger$followerStates;
    }
}
