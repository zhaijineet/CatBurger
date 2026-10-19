package net.zhaiji.catburger.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.zhaiji.catburger.client.render.FirstPersonFollowerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 让关卡渲染状态承载第一人称补渲染用的佩戴者渲染状态；数据在抽取阶段写入，提交阶段只读。
 */
@Mixin(LevelRenderState.class)
public abstract class LevelRenderStateMixin implements FirstPersonFollowerHolder {
    @Unique
    private LivingEntityRenderState catburger$firstPersonRenderState;

    @Override
    public LivingEntityRenderState catburger$firstPersonRenderState() {
        return catburger$firstPersonRenderState;
    }

    @Override
    public void catburger$setFirstPersonRenderState(LivingEntityRenderState firstPersonRenderState) {
        this.catburger$firstPersonRenderState = firstPersonRenderState;
    }
}
