package net.zhaiji.catburger.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerCreativeModeTabs;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.util.TooltipUtil;
import org.jetbrains.annotations.Nullable;

public class CatBurgerLanguageProvider extends LanguageProvider {
    public static final String EN_US = "en_us";
    public static final String ZH_CN = "zh_cn";
    public static final String JA_JP = "ja_jp";

    public final String locale;

    public CatBurgerLanguageProvider(PackOutput output, String locale) {
        super(output, CatBurger.MOD_ID, locale);
        this.locale = locale;
    }

    public void English() {
        add(CatBurgerCreativeModeTabs.CATBURGER_TAB_TRANSLATABLE, "Cat Burger");
        addItem(CatBurgerItems.CAT_BURGER, "Cat Burger");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "Hold [%s] for details");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "description.0", "Meow!");
        add(itemKeyPrefix + "effect.food_level_restoration", "While worn, restores %2$s food level every %1$s seconds, up to %3$s");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "While worn, restores %s food level every tick, up to %s");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "On fatal damage, triggers death protection and restores health to %s%% of max health");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "On fatal damage, triggers death protection and restores health to %s");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", ", food level to %s and saturation to %s");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "Death protection cooldown: %s seconds");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "Waking up from sleep resets the death protection cooldown");
        add(itemKeyPrefix + "effect.death_protection.disabled", "Death protection is disabled in the config");

        addConfig("title", "Cat Burger Configuration");

        addConfig("DeathProtectionCooldown", "Death Protection Cooldown", "Death protection cooldown, applies to every follower");
        addConfig("cooldownDuration", "Cooldown Duration", "Ticks before death protection can trigger again, 0 disables the cooldown");
        addConfig("resetCooldownOnWakeUp", "Reset Cooldown on Wake Up", "Waking up from sleep resets the cooldown");

        addConfig("Followers", "Followers", "Settings for each follower");
        addConfig("CatBurger", "Cat Burger", "Follower that restores food and protects from death while worn");
        addConfig("TickEffects", "Tick Effects", "Effects that trigger repeatedly while the follower is worn");
        addConfig("foodLevelRestorationInterval", "Food Level Restoration Interval", "Ticks between restorations, 0 restores every tick");
        addConfig("foodLevelRestorationAmount", "Food Level Restoration Amount", "Amount restored each time");
        addConfig("foodLevelRestorationLimit", "Food Level Restoration Limit", "Restoration stops once it reaches this value");
        addConfig("DeathProtectionEffect", "Death Protection Effect", "Death protection while a follower is worn");
        addConfig("deathProtectionEnabled", "Enable Death Protection", "Trigger when the wearer takes fatal damage");
        addConfig("useHealthPercentage", "Use Health Percentage", "Health is restored as a percentage of max health, otherwise to a fixed value");
        addConfig("restoreHealthPercentage", "Restore Health Percentage", "Health is restored to this percentage of max health");
        addConfig("restoreHealth", "Restore Health", "In fixed value mode, restored to this value");
        addConfig("restoreFoodLevel", "Restore Food Level", "Restored to this value");
        addConfig("restoreSaturation", "Restore Saturation", "Restored to this value");

        addConfig("MobPickup", "Mob Pickup", "Whether mobs can pick up and wear followers");
        addConfig("mobPickupEnabled", "Enable Mob Pickup", "Allow mobs to pick up and wear followers");

        addConfig("ScaleAndOffset", "Scale and Offset", "Size and position of the follower relative to the wearer's eyes; offsets follow the orbit direction set by spring motion or rotation, not the follower's facing");
        addConfig("renderScale", "Render Scale", "Multiplied by the wearer's own scale");
        addConfig("frontBackOffset", "Front/Back Offset", "Blocks to move forward along the orbit direction, negative moves backward");
        addConfig("leftRightOffset", "Left/Right Offset", "Blocks to move to the right of the orbit direction, negative moves left");
        addConfig("verticalOffset", "Vertical Offset", "Blocks to move up from eye height, negative moves down");
        addConfig("crouchOffset", "Crouch Offset", "Extra blocks to move up while sneaking, negative moves down");

        addConfig("FloatMotion", "Float Motion", "Up and down floating motion");
        addConfig("floatDistance", "Float Distance", "Height in blocks between the highest and lowest point of the float");
        addConfig("floatCycleDuration", "Float Cycle Duration", "Ticks for one full float cycle");

        addConfig("FollowDelay", "Follow Delay", "Trailing behind the wearer while moving");
        addConfig("followSpeed", "Follow Speed", "How quickly the follower catches up, higher values give a shorter trail, 1.0 follows instantly");
        addConfig("maxLagDistance", "Max Lag Distance", "Maximum distance in blocks the follower trails behind, 0 disables trailing");

        addConfig("SpringMotion", "Spring Motion", "Spring motion for orbiting, overrides rotation when enabled");
        addConfig("springMotionEnabled", "Enable Spring Motion", "Orbit with spring motion that can overshoot and bounce back");
        addConfig("springStiffness", "Spring Stiffness", "Spring stiffness, higher is snappier");
        addConfig("springDamping", "Spring Damping", "Fraction of spring speed kept each tick, lower settles faster");

        addConfig("TurningAndFacing", "Turning and Facing", "How the follower orbits and faces when the wearer turns or looks around");
        addConfig("rotationFollowSpeed", "Rotation Follow Speed", "How quickly the orbit catches up with the head, higher is faster, 1.0 follows instantly");
        addConfig("useShortestArc", "Use Shortest Arc", "Catch up along the shortest arc instead of following every full turn");
        addConfig("orientationFollowSpeed", "Orientation Follow Speed", "How quickly the facing catches up with its target, higher is faster, 1.0 follows instantly");
        addConfig("gazeAtViewTarget", "Gaze at View Target", "Aim the follower worn by the camera entity, usually yourself, at the crosshair hit point; followers worn by others are unaffected");
    }

    public void Chinese() {
        add(CatBurgerCreativeModeTabs.CATBURGER_TAB_TRANSLATABLE, "猫猫汉堡");
        addItem(CatBurgerItems.CAT_BURGER, "猫猫汉堡");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "按住[%s]查看详细说明");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "description.0", "喵!");
        add(itemKeyPrefix + "effect.food_level_restoration", "佩戴时每%s秒恢复%s点饥饿值，最多恢复至%s点");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "佩戴时每刻恢复%s点饥饿值，最多恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "受到致命伤害时触发死亡保护，生命值恢复至最大生命值的%s%%");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "受到致命伤害时触发死亡保护，生命值恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", "，饥饿值恢复至%s点，饱和度恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "死亡保护冷却时间：%s秒");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "睡醒后重置死亡保护的冷却时间");
        add(itemKeyPrefix + "effect.death_protection.disabled", "死亡保护已在配置中禁用");

        addConfig("title", "猫猫汉堡配置");

        addConfig("DeathProtectionCooldown", "死亡保护冷却", "死亡保护冷却，对所有追随者生效");
        addConfig("cooldownDuration", "冷却时长", "死亡保护再次触发所需的刻数，0为无冷却");
        addConfig("resetCooldownOnWakeUp", "睡醒时重置冷却", "佩戴者睡醒后重置冷却");

        addConfig("Followers", "追随者", "每个追随者各自的配置");
        addConfig("CatBurger", "猫猫汉堡", "佩戴时恢复饥饿值并提供死亡保护的追随者");
        addConfig("TickEffects", "Tick效果", "佩戴追随者时反复触发的效果");
        addConfig("foodLevelRestorationInterval", "饥饿值恢复间隔", "两次恢复之间的刻数，0为每刻恢复");
        addConfig("foodLevelRestorationAmount", "饥饿值恢复量", "每次恢复的数值");
        addConfig("foodLevelRestorationLimit", "饥饿值恢复上限", "达到此数值后不再恢复");
        addConfig("DeathProtectionEffect", "死亡保护效果", "佩戴追随者时的死亡保护");
        addConfig("deathProtectionEnabled", "启用死亡保护", "佩戴者受到致命伤害时触发");
        addConfig("useHealthPercentage", "使用生命值百分比", "按最大生命值的百分比恢复生命值，关闭时恢复至固定数值");
        addConfig("restoreHealthPercentage", "恢复生命值百分比", "生命值恢复至最大生命值的此百分比");
        addConfig("restoreHealth", "恢复生命值", "固定数值模式下恢复至此数值");
        addConfig("restoreFoodLevel", "恢复饥饿值", "恢复至此数值");
        addConfig("restoreSaturation", "恢复饱和度", "恢复至此数值");

        addConfig("MobPickup", "生物拾取", "生物是否能捡起并佩戴追随者");
        addConfig("mobPickupEnabled", "启用生物拾取", "允许生物捡起并佩戴追随者");

        addConfig("ScaleAndOffset", "缩放与偏移", "追随者相对佩戴者眼部的大小与位置；偏移沿环绕方向计算，环绕方向由弹簧运动或转向决定，与追随者自身朝向无关");
        addConfig("renderScale", "渲染缩放", "与佩戴者自身缩放相乘");
        addConfig("frontBackOffset", "前后偏移", "沿环绕方向向前偏移的格数，负值向后");
        addConfig("leftRightOffset", "左右偏移", "沿环绕方向向右偏移的格数，负值向左");
        addConfig("verticalOffset", "垂直偏移", "相对眼部高度向上偏移的格数，负值向下");
        addConfig("crouchOffset", "蹲伏偏移", "潜行时额外向上偏移的格数，负值向下");

        addConfig("FloatMotion", "浮动运动", "上下浮动动画");
        addConfig("floatDistance", "浮动距离", "浮动最高点与最低点之间相差的格数");
        addConfig("floatCycleDuration", "浮动周期时长", "完成一次完整浮动所需的刻数");

        addConfig("FollowDelay", "延迟跟随", "佩戴者移动时追随者落后跟随");
        addConfig("followSpeed", "跟随速度", "追随者追上佩戴者的速度，越大拖尾越短，1.0为瞬时跟随");
        addConfig("maxLagDistance", "最大滞后距离", "追随者最多落后的格数，0为不拖尾");

        addConfig("SpringMotion", "弹簧运动", "以弹簧运动环绕跟随，启用时覆盖转向");
        addConfig("springMotionEnabled", "启用弹簧运动", "以可能越过并回弹的弹簧运动环绕跟随");
        addConfig("springStiffness", "弹簧刚度", "弹簧刚度，越大回弹越快");
        addConfig("springDamping", "弹簧阻尼", "每刻保留的弹簧速度比例，越小越快静止");

        addConfig("TurningAndFacing", "转向与朝向", "佩戴者转头或环顾时追随者如何环绕和朝向");
        addConfig("rotationFollowSpeed", "转向跟随速度", "环绕追上头部朝向的速度，越大越快，1.0为瞬时跟随");
        addConfig("useShortestArc", "使用最短路径", "沿最短弧线追上头部朝向，不再跟随每一圈完整转身");
        addConfig("orientationFollowSpeed", "朝向跟随速度", "朝向追上目标的速度，越大越快，1.0为瞬时跟随");
        addConfig("gazeAtViewTarget", "注视视线终点", "让当前视角所属实体佩戴的追随者注视准心命中点，通常就是你自己，其他人佩戴的不受影响");
    }

    public void Japanese() {
        add(CatBurgerCreativeModeTabs.CATBURGER_TAB_TRANSLATABLE, "猫バーガー");
        addItem(CatBurgerItems.CAT_BURGER, "猫バーガー");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "[%s]を押して詳細を表示");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "description.0", "にゃー!");
        add(itemKeyPrefix + "effect.food_level_restoration", "装備中、%s秒ごとに満腹度を%s回復し、最大%sまで回復する");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "装備中、毎tick満腹度を%s回復し、最大%sまで回復する");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "致死ダメージを受けると死亡保護が発動し、体力が最大体力の%s%%まで回復");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "致死ダメージを受けると死亡保護が発動し、体力が%sまで回復");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", "、満腹度が%s、隠し満腹度が%sまで回復");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "死亡保護のクールダウン：%s秒");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "起床すると死亡保護のクールダウンがリセットされる");
        add(itemKeyPrefix + "effect.death_protection.disabled", "死亡保護は設定で無効になっている");

        addConfig("title", "猫バーガーの設定");

        addConfig("DeathProtectionCooldown", "死亡保護のクールダウン", "死亡保護のクールダウン、全てのフォロワーに適用");
        addConfig("cooldownDuration", "クールダウン時間", "死亡保護が再び発動するまでのtick数、0でクールダウンなし");
        addConfig("resetCooldownOnWakeUp", "起床時にクールダウンをリセット", "装備者が起床するとクールダウンがリセットされる");

        addConfig("Followers", "フォロワー", "フォロワーごとの設定");
        addConfig("CatBurger", "猫バーガー", "装備中は満腹度を回復し死亡保護を行うフォロワー");
        addConfig("TickEffects", "Tick効果", "フォロワー装備中に繰り返し発動する効果");
        addConfig("foodLevelRestorationInterval", "満腹度回復の間隔", "回復の間隔となるtick数、0で毎tick回復");
        addConfig("foodLevelRestorationAmount", "満腹度回復量", "1回ごとに回復する量");
        addConfig("foodLevelRestorationLimit", "満腹度回復の上限", "この値に達すると回復しない");
        addConfig("DeathProtectionEffect", "死亡保護効果", "フォロワー装備中の死亡保護");
        addConfig("deathProtectionEnabled", "死亡保護を有効化", "装備者が致死ダメージを受けると発動する");
        addConfig("useHealthPercentage", "体力割合を使用", "最大体力に対する割合で回復し、無効時は固定値まで回復");
        addConfig("restoreHealthPercentage", "回復体力の割合", "体力が最大体力のこの割合まで回復する");
        addConfig("restoreHealth", "体力を回復", "固定値モードではこの値まで回復する");
        addConfig("restoreFoodLevel", "満腹度を回復", "この値まで回復する");
        addConfig("restoreSaturation", "隠し満腹度を回復", "この値まで回復する");

        addConfig("MobPickup", "Mobの拾得", "Mobがフォロワーを拾って装備できるかどうか");
        addConfig("mobPickupEnabled", "Mobの拾得を有効化", "Mobがフォロワーを拾って装備できるようにする");

        addConfig("ScaleAndOffset", "スケールとオフセット", "装備者の目の位置を基準としたフォロワーの大きさと位置；オフセットはバネ運動または回転で決まる周回方向に沿い、フォロワー自身の向きとは無関係");
        addConfig("renderScale", "描画倍率", "装備者自身のスケールに乗算される");
        addConfig("frontBackOffset", "前後オフセット", "周回方向へ前に動かすブロック数、負の値で後ろ");
        addConfig("leftRightOffset", "左右オフセット", "周回方向の右に動かすブロック数、負の値で左");
        addConfig("verticalOffset", "垂直オフセット", "目の高さから上に動かすブロック数、負の値で下");
        addConfig("crouchOffset", "しゃがみ時オフセット", "スニーク中に追加で上に動かすブロック数、負の値で下");

        addConfig("FloatMotion", "浮遊運動", "上下に揺れる浮遊アニメーション");
        addConfig("floatDistance", "浮遊距離", "浮遊の最高点と最低点の差のブロック数");
        addConfig("floatCycleDuration", "浮遊周期時間", "浮遊が1周するのにかかるtick数");

        addConfig("FollowDelay", "追従遅延", "装備者の移動時にフォロワーが遅れて追従する");
        addConfig("followSpeed", "追従速度", "フォロワーが追いつく速さ、大きいほど遅れが短い、1.0で即座に追従");
        addConfig("maxLagDistance", "最大遅延距離", "フォロワーが遅れる最大のブロック数、0で遅延なし");

        addConfig("SpringMotion", "バネ運動", "バネ運動で周回する、有効時は回転より優先");
        addConfig("springMotionEnabled", "バネ運動を有効化", "行き過ぎて跳ね返るバネ運動で周回する");
        addConfig("springStiffness", "バネの硬さ", "バネの硬さ、大きいほど素早く戻る");
        addConfig("springDamping", "バネの減衰", "毎tick保持されるバネの速度の割合、小さいほど早く静止");

        addConfig("TurningAndFacing", "回転と向き", "装備者が振り向いたり見回したりした時のフォロワーの周回と向き");
        addConfig("rotationFollowSpeed", "回転の追従速度", "周回が頭の向きに追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("useShortestArc", "最短経路を使用", "1周ごとに追従せず、最短の弧で頭の向きに追いつく");
        addConfig("orientationFollowSpeed", "向きの追従速度", "向きが目標に追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("gazeAtViewTarget", "視線の終点を注視", "カメラの視点となるエンティティのフォロワーを照準点に向けさせ、通常は自分のフォロワーで、他者のフォロワーには影響しない");
    }

    /**
     * 添加 ConfigurationScreen 配置项或分组的名称与可选说明。
     */
    private void addConfig(String key, String name, @Nullable String tooltip) {
        add(CatBurger.MOD_ID + ".configuration." + key, name);
        if (tooltip != null) add(CatBurger.MOD_ID + ".configuration." + key + ".tooltip", tooltip);
    }

    private void addConfig(String key, String name) {
        addConfig(key, name, null);
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case EN_US -> English();
            case ZH_CN -> Chinese();
            case JA_JP -> Japanese();
        }
    }
}
