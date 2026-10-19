package net.zhaiji.catburger.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * 由关卡渲染状态侧 mixin 实现，承载抽取阶段为第一人称补渲染算好的佩戴者渲染状态。
 */
public interface FirstPersonFollowerHolder {
    LivingEntityRenderState catburger$firstPersonRenderState();

    void catburger$setFirstPersonRenderState(LivingEntityRenderState firstPersonRenderState);
}
