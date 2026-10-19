package net.zhaiji.catburger.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.zhaiji.catburger.attachment.FollowerRenderData;
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

    @Unique
    private FollowerRenderData catburger$layoutParams;

    @Unique
    private double catburger$layoutDisplaySize;

    @Unique
    private double[] catburger$layout = new double[0];

    @Override
    public Map<String, FollowerState> catburger$followerStates() {
        return catburger$followerStates;
    }

    @Override
    public FollowerRenderData catburger$layoutParams() {
        return catburger$layoutParams;
    }

    @Override
    public double catburger$layoutDisplaySize() {
        return catburger$layoutDisplaySize;
    }

    @Override
    public double[] catburger$layout() {
        return catburger$layout;
    }

    @Override
    public void catburger$setLayout(FollowerRenderData renderParams, double displaySize, double[] layout) {
        this.catburger$layoutParams = renderParams;
        this.catburger$layoutDisplaySize = displaySize;
        this.catburger$layout = layout;
    }
}
