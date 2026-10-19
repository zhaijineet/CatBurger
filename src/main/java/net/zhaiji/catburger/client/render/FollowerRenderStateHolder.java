package net.zhaiji.catburger.client.render;

import net.zhaiji.catburger.attachment.FollowerRenderData;

import java.util.List;

/**
 * 由渲染状态侧 mixin 实现，承载抽取阶段算好的跟随者数据，使提交阶段只读状态、不回查实体。
 */
public interface FollowerRenderStateHolder {
    List<FollowerState> catburger$followerStates();

    void catburger$setFollowerStates(List<FollowerState> followerStates);

    float catburger$entityScale();

    void catburger$setEntityScale(float entityScale);

    float catburger$entityBbWidth();

    void catburger$setEntityBbWidth(float entityBbWidth);

    FollowerState catburger$formationState();

    void catburger$setFormationState(FollowerState formationState);

    FollowerRenderData catburger$renderParams();

    void catburger$setRenderParams(FollowerRenderData renderParams);

    boolean catburger$preview();

    void catburger$setPreview(boolean preview);

    boolean catburger$headIsFollower();

    void catburger$setHeadIsFollower(boolean headIsFollower);

    double catburger$displaySize();

    void catburger$setDisplaySize(double displaySize);

    double[] catburger$layout();

    void catburger$setLayout(double[] layout);

    double catburger$x();

    void catburger$setX(double x);

    double catburger$y();

    void catburger$setY(double y);

    double catburger$z();

    void catburger$setZ(double z);

    int catburger$tickCount();

    void catburger$setTickCount(int tickCount);

    float catburger$partialTick();

    void catburger$setPartialTick(float partialTick);
}
