package net.zhaiji.catburger.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.zhaiji.catburger.attachment.FollowerRenderData;
import net.zhaiji.catburger.client.render.FollowerRenderStateHolder;
import net.zhaiji.catburger.client.render.FollowerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * 让渲染状态自带抽取阶段算好的跟随者数据；渲染状态按渲染器复用，字段每次抽取覆盖。
 * 位置、刻数与角度取刻末值，不用渲染状态自带的帧插值字段，因为跟随者的拖尾按刻推进。
 */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements FollowerRenderStateHolder {
    @Unique
    private List<FollowerState> catburger$followerStates;

    // 渲染状态自带的scale与boundingBoxWidth在界面预览里被改成1.0与显示宽，跟随者要按实体真实值换算，因此另存一份
    @Unique
    private float catburger$entityScale;

    @Unique
    private float catburger$entityBbWidth;

    @Unique
    private FollowerState catburger$formationState;

    @Unique
    private FollowerRenderData catburger$renderParams;

    @Unique
    private boolean catburger$preview;

    @Unique
    private boolean catburger$headIsFollower;

    @Unique
    private double catburger$displaySize;

    @Unique
    private double[] catburger$layout = new double[0];

    @Unique
    private double catburger$x;

    @Unique
    private double catburger$y;

    @Unique
    private double catburger$z;

    @Unique
    private int catburger$tickCount;

    @Unique
    private float catburger$partialTick;

    @Override
    public List<FollowerState> catburger$followerStates() {
        return catburger$followerStates;
    }

    @Override
    public void catburger$setFollowerStates(List<FollowerState> followerStates) {
        this.catburger$followerStates = followerStates;
    }

    @Override
    public float catburger$entityScale() {
        return catburger$entityScale;
    }

    @Override
    public void catburger$setEntityScale(float entityScale) {
        this.catburger$entityScale = entityScale;
    }

    @Override
    public float catburger$entityBbWidth() {
        return catburger$entityBbWidth;
    }

    @Override
    public void catburger$setEntityBbWidth(float entityBbWidth) {
        this.catburger$entityBbWidth = entityBbWidth;
    }

    @Override
    public FollowerState catburger$formationState() {
        return catburger$formationState;
    }

    @Override
    public void catburger$setFormationState(FollowerState formationState) {
        this.catburger$formationState = formationState;
    }

    @Override
    public FollowerRenderData catburger$renderParams() {
        return catburger$renderParams;
    }

    @Override
    public void catburger$setRenderParams(FollowerRenderData renderParams) {
        this.catburger$renderParams = renderParams;
    }

    @Override
    public boolean catburger$preview() {
        return catburger$preview;
    }

    @Override
    public void catburger$setPreview(boolean preview) {
        this.catburger$preview = preview;
    }

    @Override
    public boolean catburger$headIsFollower() {
        return catburger$headIsFollower;
    }

    @Override
    public void catburger$setHeadIsFollower(boolean headIsFollower) {
        this.catburger$headIsFollower = headIsFollower;
    }

    @Override
    public double catburger$displaySize() {
        return catburger$displaySize;
    }

    @Override
    public void catburger$setDisplaySize(double displaySize) {
        this.catburger$displaySize = displaySize;
    }

    @Override
    public double[] catburger$layout() {
        return catburger$layout;
    }

    @Override
    public void catburger$setLayout(double[] layout) {
        this.catburger$layout = layout;
    }

    @Override
    public double catburger$x() {
        return catburger$x;
    }

    @Override
    public void catburger$setX(double x) {
        this.catburger$x = x;
    }

    @Override
    public double catburger$y() {
        return catburger$y;
    }

    @Override
    public void catburger$setY(double y) {
        this.catburger$y = y;
    }

    @Override
    public double catburger$z() {
        return catburger$z;
    }

    @Override
    public void catburger$setZ(double z) {
        this.catburger$z = z;
    }

    @Override
    public int catburger$tickCount() {
        return catburger$tickCount;
    }

    @Override
    public void catburger$setTickCount(int tickCount) {
        this.catburger$tickCount = tickCount;
    }

    @Override
    public float catburger$partialTick() {
        return catburger$partialTick;
    }

    @Override
    public void catburger$setPartialTick(float partialTick) {
        this.catburger$partialTick = partialTick;
    }
}
