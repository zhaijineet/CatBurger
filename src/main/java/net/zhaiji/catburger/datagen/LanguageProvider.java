package net.zhaiji.catburger.datagen;

import net.minecraft.data.PackOutput;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.init.CatBurgerCreativeModeTabs;
import net.zhaiji.catburger.init.CatBurgerItems;
import net.zhaiji.catburger.util.TooltipUtil;
import org.jetbrains.annotations.Nullable;

public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {
    public static final String EN_US = "en_us";
    public static final String ZH_CN = "zh_cn";
    public static final String JA_JP = "ja_jp";

    public final String locale;

    public LanguageProvider(PackOutput output, String locale) {
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

        addConfig("DeathProtection", "Death Protection", "Death protection while a cat burger is worn");
        addConfig("deathProtectionEnabled", "Enable Death Protection", "Trigger death protection when the wearer takes fatal damage");
        addConfig("cooldown", "Cooldown", "Ticks to wait before death protection can trigger again, 0 disables the cooldown");
        addConfig("useHealthPercentage", "Use Health Percentage", "When enabled, health is restored as a percentage of max health, otherwise to a fixed value");
        addConfig("restoreHealthPercentage", "Restore Health Percentage", "In percentage mode, health is restored to this percentage of max health");
        addConfig("restoreHealth", "Restore Health", "In fixed value mode, health is restored to this value");
        addConfig("restoreFoodLevel", "Restore Food Level", "Food level is restored to this value when death protection triggers");
        addConfig("restoreSaturation", "Restore Saturation", "Saturation is restored to this value when death protection triggers");
        addConfig("resetCooldownOnWakeUp", "Reset Cooldown on Wake Up", "Waking up from sleep resets the wearer's death protection cooldown");

        addConfig("FoodLevelRestoration", "Food Level Restoration", "Food level restoration while a cat burger is worn");
        addConfig("interval", "Interval", "Ticks between food level restorations, 0 restores every tick");
        addConfig("amount", "Amount", "Food level restored each time");
        addConfig("limit", "Limit", "Food level stops being restored once it reaches this value");

        addConfig("MobPickup", "Mob Pickup", "Whether mobs can pick up and wear cat burgers");
        addConfig("mobPickupEnabled", "Enable Mob Pickup", "Allow mobs to pick up and wear cat burgers");

        addConfig("Position", "Position", "Size and position of the cat burger relative to the wearer's eyes; offsets follow the orbit direction set by spring motion or rotation, not the cat burger's facing");
        addConfig("scale", "Scale", "Render scale of the cat burger");
        addConfig("frontBackOffset", "Front/Back Offset", "Blocks to move forward along the orbit direction, negative moves backward");
        addConfig("leftRightOffset", "Left/Right Offset", "Blocks to move to the right of the orbit direction, negative moves left");
        addConfig("verticalOffset", "Vertical Offset", "Blocks to move up from eye height, negative moves down");
        addConfig("crouchOffset", "Crouch Offset", "Extra blocks to move up while sneaking, negative moves down");

        addConfig("Float", "Float", "Up and down floating motion");
        addConfig("floatDistance", "Float Distance", "Height in blocks between the highest and lowest point of the float");
        addConfig("floatCycleDuration", "Float Cycle Duration", "Ticks for one full float cycle");

        addConfig("FollowDelay", "Follow Delay", "Trailing behind the wearer while moving");
        addConfig("followSpeed", "Follow Speed", "How quickly the cat burger catches up, higher values give a shorter trail, 1.0 follows instantly");
        addConfig("maxLagDistance", "Max Lag Distance", "Maximum distance in blocks the cat burger trails behind, 0 disables trailing");

        addConfig("SpringMotion", "Spring Motion", "Spring motion for orbiting, overrides rotation when enabled");
        addConfig("springMotionEnabled", "Enable Spring Motion", "Orbit with spring motion that can overshoot and bounce back");
        addConfig("stiffness", "Stiffness", "Spring stiffness, higher is snappier");
        addConfig("damping", "Damping", "Fraction of spring speed kept each tick, lower settles faster");

        addConfig("Turning", "Turning", "How the cat burger orbits and faces when the wearer turns or looks around");
        addConfig("rotationFollowSpeed", "Rotation Follow Speed", "How quickly the orbit catches up with the head, higher is faster, 1.0 follows instantly");
        addConfig("useShortestArc", "Use Shortest Arc", "Catch up along the shortest arc instead of following every full turn");
        addConfig("orientationFollowSpeed", "Orientation Follow Speed", "How quickly the facing catches up with its target, higher is faster, 1.0 follows instantly");
        addConfig("gazeAtViewTarget", "Gaze at View Target", "Aim the cat burger worn by the camera entity, usually yourself, at the crosshair hit point; cat burgers worn by others are unaffected");
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

        addConfig("DeathProtection", "死亡保护", "佩戴猫猫汉堡时的死亡保护效果");
        addConfig("deathProtectionEnabled", "启用死亡保护", "佩戴者受到致命伤害时触发死亡保护");
        addConfig("cooldown", "冷却时间", "死亡保护触发后再次可用所需的刻数，0为无冷却");
        addConfig("useHealthPercentage", "使用生命值百分比", "开启时按最大生命值的百分比恢复生命值，关闭时恢复至固定数值");
        addConfig("restoreHealthPercentage", "恢复生命值百分比", "百分比模式下，生命值恢复至最大生命值的此百分比");
        addConfig("restoreHealth", "恢复生命值", "固定数值模式下，生命值恢复至此数值");
        addConfig("restoreFoodLevel", "恢复饥饿值", "死亡保护触发时饥饿值恢复至此数值");
        addConfig("restoreSaturation", "恢复饱和度", "死亡保护触发时饱和度恢复至此数值");
        addConfig("resetCooldownOnWakeUp", "睡醒时重置冷却", "佩戴者睡醒后重置死亡保护的冷却时间");

        addConfig("FoodLevelRestoration", "饥饿值恢复", "佩戴猫猫汉堡时的饥饿值恢复效果");
        addConfig("interval", "间隔", "每隔多少刻恢复一次饥饿值，0为每刻恢复");
        addConfig("amount", "恢复量", "每次恢复的饥饿值");
        addConfig("limit", "上限", "饥饿值达到此数值后不再恢复");

        addConfig("MobPickup", "生物拾取", "生物是否能捡起并佩戴猫猫汉堡");
        addConfig("mobPickupEnabled", "启用生物拾取", "允许生物捡起并佩戴猫猫汉堡");

        addConfig("Position", "位置", "猫猫汉堡相对佩戴者眼部的大小与位置；偏移沿环绕方向计算，环绕方向由弹簧运动或转向决定，与汉堡自身朝向无关");
        addConfig("scale", "缩放", "猫猫汉堡的渲染缩放");
        addConfig("frontBackOffset", "前后偏移", "沿环绕方向向前偏移的格数，负值向后");
        addConfig("leftRightOffset", "左右偏移", "沿环绕方向向右偏移的格数，负值向左");
        addConfig("verticalOffset", "垂直偏移", "相对眼部高度向上偏移的格数，负值向下");
        addConfig("crouchOffset", "蹲伏偏移", "潜行时额外向上偏移的格数，负值向下");

        addConfig("Float", "浮动", "上下浮动动画");
        addConfig("floatDistance", "浮动距离", "浮动最高点与最低点之间相差的格数");
        addConfig("floatCycleDuration", "浮动周期时长", "完成一次完整浮动所需的刻数");

        addConfig("FollowDelay", "延迟跟随", "佩戴者移动时猫猫汉堡落后跟随");
        addConfig("followSpeed", "跟随速度", "猫猫汉堡追上佩戴者的速度，越大拖尾越短，1.0为瞬时跟随");
        addConfig("maxLagDistance", "最大滞后距离", "猫猫汉堡最多落后的格数，0为不拖尾");

        addConfig("SpringMotion", "弹簧运动", "以弹簧运动环绕跟随，启用时覆盖转向");
        addConfig("springMotionEnabled", "启用弹簧运动", "以可能越过并回弹的弹簧运动环绕跟随");
        addConfig("stiffness", "刚度", "弹簧刚度，越大回弹越快");
        addConfig("damping", "阻尼", "每刻保留的弹簧速度比例，越小越快静止");

        addConfig("Turning", "转向与朝向", "佩戴者转头或环顾时猫猫汉堡如何环绕和朝向");
        addConfig("rotationFollowSpeed", "转向跟随速度", "环绕追上头部朝向的速度，越大越快，1.0为瞬时跟随");
        addConfig("useShortestArc", "使用最短路径", "沿最短弧线追上头部朝向，不再跟随每一圈完整转身");
        addConfig("orientationFollowSpeed", "朝向跟随速度", "朝向追上目标的速度，越大越快，1.0为瞬时跟随");
        addConfig("gazeAtViewTarget", "注视视线终点", "让当前视角所属实体佩戴的猫猫汉堡注视准心命中点，通常就是你自己，其他人佩戴的不受影响");
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

        addConfig("DeathProtection", "死亡保護", "猫バーガー装備中の死亡保護");
        addConfig("deathProtectionEnabled", "死亡保護を有効化", "装備者が致死ダメージを受けると死亡保護が発動する");
        addConfig("cooldown", "クールダウン", "死亡保護が発動してから再び使えるまでのtick数、0でクールダウンなし");
        addConfig("useHealthPercentage", "体力割合を使用", "有効時は最大体力に対する割合、無効時は固定値まで体力を回復");
        addConfig("restoreHealthPercentage", "回復体力の割合", "割合モードでは、体力が最大体力のこの割合まで回復する");
        addConfig("restoreHealth", "体力を回復", "固定値モードでは、体力がこの値まで回復する");
        addConfig("restoreFoodLevel", "満腹度を回復", "死亡保護が発動すると満腹度がこの値まで回復する");
        addConfig("restoreSaturation", "隠し満腹度を回復", "死亡保護が発動すると隠し満腹度がこの値まで回復する");
        addConfig("resetCooldownOnWakeUp", "起床時にクールダウンをリセット", "装備者が起床すると死亡保護のクールダウンがリセットされる");

        addConfig("FoodLevelRestoration", "満腹度回復", "猫バーガー装備中の満腹度回復");
        addConfig("interval", "間隔", "満腹度を回復する間隔のtick数、0で毎tick回復");
        addConfig("amount", "回復量", "1回ごとに回復する満腹度");
        addConfig("limit", "上限", "満腹度がこの値に達すると回復しない");

        addConfig("MobPickup", "Mobの拾得", "Mobが猫バーガーを拾って装備できるかどうか");
        addConfig("mobPickupEnabled", "Mobの拾得を有効化", "Mobが猫バーガーを拾って装備できるようにする");

        addConfig("Position", "位置", "装備者の目の位置を基準とした猫バーガーの大きさと位置；オフセットはバネ運動または回転で決まる周回方向に沿い、猫バーガー自身の向きとは無関係");
        addConfig("scale", "スケール", "猫バーガーの描画倍率");
        addConfig("frontBackOffset", "前後オフセット", "周回方向へ前に動かすブロック数、負の値で後ろ");
        addConfig("leftRightOffset", "左右オフセット", "周回方向の右に動かすブロック数、負の値で左");
        addConfig("verticalOffset", "垂直オフセット", "目の高さから上に動かすブロック数、負の値で下");
        addConfig("crouchOffset", "しゃがみ時オフセット", "スニーク中に追加で上に動かすブロック数、負の値で下");

        addConfig("Float", "浮遊", "上下に揺れる浮遊アニメーション");
        addConfig("floatDistance", "浮遊距離", "浮遊の最高点と最低点の差のブロック数");
        addConfig("floatCycleDuration", "浮遊周期時間", "浮遊が1周するのにかかるtick数");

        addConfig("FollowDelay", "追従遅延", "装備者の移動時に猫バーガーが遅れて追従する");
        addConfig("followSpeed", "追従速度", "猫バーガーが追いつく速さ、大きいほど遅れが短い、1.0で即座に追従");
        addConfig("maxLagDistance", "最大遅延距離", "猫バーガーが遅れる最大のブロック数、0で遅延なし");

        addConfig("SpringMotion", "バネ運動", "バネ運動で周回する、有効時は回転より優先");
        addConfig("springMotionEnabled", "バネ運動を有効化", "行き過ぎて跳ね返るバネ運動で周回する");
        addConfig("stiffness", "硬さ", "バネの硬さ、大きいほど素早く戻る");
        addConfig("damping", "減衰", "毎tick保持されるバネの速度の割合、小さいほど早く静止");

        addConfig("Turning", "回転と向き", "装備者が振り向いたり見回したりした時の猫バーガーの周回と向き");
        addConfig("rotationFollowSpeed", "回転の追従速度", "周回が頭の向きに追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("useShortestArc", "最短経路を使用", "1周ごとに追従せず、最短の弧で頭の向きに追いつく");
        addConfig("orientationFollowSpeed", "向きの追従速度", "向きが目標に追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("gazeAtViewTarget", "視線の終点を注視", "カメラの視点となるエンティティの猫バーガーを照準点に向けさせる、通常は自分の猫バーガーで、他者の猫バーガーには影響しない");
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
