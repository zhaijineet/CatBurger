package net.zhaiji.catburger.client.render;

import java.util.Map;

/**
 * 由实体侧 mixin 实现，提供该实体身上按槽位分列的跟随者状态。
 */
public interface FollowerStateHolder {
    Map<String, FollowerState> catburger$followerStates();
}
