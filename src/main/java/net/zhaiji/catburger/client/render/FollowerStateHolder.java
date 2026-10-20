package net.zhaiji.catburger.client.render;

import net.zhaiji.catburger.capability.FollowerRenderData;

import java.util.Map;

/**
 * 由实体侧 mixin 实现，提供该实体身上按槽位分列的跟随者状态，以及独立模式复用的散布布局。
 */
public interface FollowerStateHolder {
    Map<String, FollowerState> catburger$followerStates();

    FollowerRenderData catburger$layoutParams();

    int catburger$layoutRevision();

    double catburger$layoutDisplaySize();

    double[] catburger$layout();

    void catburger$setLayout(FollowerRenderData renderParams, double displaySize, double[] layout);
}
