package net.zhaiji.catburger.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.config.DeduplicateMode;
import net.zhaiji.catburger.config.EntityDataFallback;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;
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
        addItem(CatBurgerItems.FOLLOWER_CONFIG_CARD, "Follower Config Card");
        String cardKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.FOLLOWER_CONFIG_CARD.get());
        add(cardKeyPrefix + "mode", "Current mode: ");
        add(cardKeyPrefix + "mode.add", "Add");
        add(cardKeyPrefix + "mode.remove", "Remove");
        add(cardKeyPrefix + "mode.wear", "Wear");
        add(cardKeyPrefix + "hud", "Follower Config Card: ");
        add(cardKeyPrefix + "effect.switch_mode", "Sneak scroll: switch mode");
        add(cardKeyPrefix + "effect.pet_sync_notice", "Pet config sync is enabled; settings on this card take priority over pet inheritance");
        add(cardKeyPrefix + "effect.maid_death_protection", "Death protection does not apply when wearing followers on other creatures");
        add(cardKeyPrefix + "effect.maid_compat", "; Touhou Little Maid has already adapted to this");
        add(cardKeyPrefix + "effect.prefix.add", "Add mode");
        add(cardKeyPrefix + "effect.prefix.remove", "Remove mode");
        add(cardKeyPrefix + "effect.prefix.wear", "Wear mode");
        add(cardKeyPrefix + "effect.add.write", ": right-click a creature to write the stored settings onto it");
        add(cardKeyPrefix + "effect.add.copy", ": sneak right-click to copy your own settings into the card");
        add(cardKeyPrefix + "effect.remove.clear", ": right-click a creature to clear its settings");
        add(cardKeyPrefix + "effect.remove.reset", ": sneak right-click to clear the settings stored in the card");
        add(cardKeyPrefix + "effect.wear.put", ": hold a follower in the off hand and right-click to put it on the creature");
        add(cardKeyPrefix + "effect.wear.take", ": sneak right-click to take the follower off the creature");
        add(cardKeyPrefix + "applied", "Applied the settings to the target");
        add(cardKeyPrefix + "removed", "Cleared the settings of the target");
        add(cardKeyPrefix + "copied", "Copied your render settings into the card");
        add(cardKeyPrefix + "cleared", "Cleared the settings stored in the card");
        add(cardKeyPrefix + "copy_missing", "You have no render settings to copy yet");
        add(cardKeyPrefix + "empty", "The card has no settings stored");
        add(cardKeyPrefix + "reject.player", "Cannot be used on players");
        add(cardKeyPrefix + "reject.owned", "This creature belongs to someone else");
        add(cardKeyPrefix + "wear.applied", "Put the follower on the target");
        add(cardKeyPrefix + "wear.removed", "Took the follower off the target");
        add(cardKeyPrefix + "wear.not_follower", "Hold a follower in the off hand first");
        add(cardKeyPrefix + "wear.empty", "The target is not wearing a follower");
        add(cardKeyPrefix + "wear.locked", "Binding curse or similar effects keep the target's head slot locked");
        addItem(CatBurgerItems.CAT_BURGER, "Cat Burger");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "Hold [%s] for details");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "flavor_text", "Meow!");
        add(itemKeyPrefix + "effect.food_level_restoration", "While worn, restores %2$s food level every %1$s seconds, up to %3$s");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "While worn, restores %s food level every tick, up to %s");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "On fatal damage, triggers death protection and restores health to %s%% of max health");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "On fatal damage, triggers death protection and restores health to %s");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", ", food level to %s and saturation to %s");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "Death protection cooldown: %s seconds");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "Waking up from sleep resets the death protection cooldown");

        addItem(CatBurgerItems.BLUB, "Jelly Bunny");

        String blubKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.BLUB.get());
        add(blubKeyPrefix + "flavor_text", "Blub!");
        add(blubKeyPrefix + "effect.tick_effects.entry", "While worn, grants %2$s (%3$s seconds) every %1$s seconds");
        add(blubKeyPrefix + "effect.tick_effects.entry.every_tick", "While worn, grants %1$s (%2$s seconds) every tick");
        add(blubKeyPrefix + "effect.death_protection.restore_health_percentage", "On fatal damage, triggers death protection and restores health to %s%% of max health");
        add(blubKeyPrefix + "effect.death_protection.restore_health", "On fatal damage, triggers death protection and restores health to %s");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_all", ", applies %2$s to mobs within %1$s blocks");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_excluded", ", applies %3$s to mobs within %1$s blocks except %2$s");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope", ", applies %3$s to %2$s within %1$s blocks");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.item", "%1$s (%2$s seconds)");
        add(blubKeyPrefix + "effect.list_separator", ", ");
        add(blubKeyPrefix + "effect.target.self", "yourself");
        add(blubKeyPrefix + "effect.target.players", "players");
        add(blubKeyPrefix + "effect.target.pets", "tamed pets");
        add(blubKeyPrefix + "effect.death_protection.cooldown", "Death protection cooldown: %s seconds");
        add(blubKeyPrefix + "effect.death_protection.wake_up", "Waking up from sleep resets the death protection cooldown");

        addConfig("title", "Cat Burger Configuration");

        addConfig("petConfigSyncEnabled", "Pet Config Sync", "When enabled, the render settings of a player are copied to their pets; the server walks every level and entity on every config change, which can lag with many players and mobs, enable with care");
        addConfig("mobActiveWearEnabled", "Enable Mob Active Wearing", "Allow mobs to actively wear followers");

        addConfigGroup("DeathProtectionCooldown", "Death Protection Cooldown", "Death protection cooldown, applies to every follower");
        addConfig("cooldownDuration", "Cooldown Duration", "Ticks before death protection can trigger again, 0 disables the cooldown");
        addConfig("resetCooldownOnWakeUp", "Reset Cooldown on Wake Up", "Waking up from sleep resets the cooldown");

        addConfigGroup("Followers", "Followers", "Settings for each follower");
        addConfigGroup("CatBurger", "Cat Burger", "Follower that restores food and protects from death while worn");
        addConfigGroup("TickEffects", "Tick Effects", "Effects that trigger repeatedly while the follower is worn");
        addConfig("foodLevelRestorationInterval", "Food Level Restoration Interval", "Ticks between restorations, 0 restores every tick");
        addConfig("foodLevelRestorationAmount", "Food Level Restoration Amount", "Amount restored each time");
        addConfig("foodLevelRestorationLimit", "Food Level Restoration Limit", "Restoration stops once it reaches this value");
        addConfigGroup("DeathProtectionEffect", "Death Protection Effect", "Death protection while a follower is worn");
        addConfig("deathProtectionEnabled", "Enable Death Protection", "Trigger when the wearer takes fatal damage");
        addConfig("useHealthPercentage", "Use Health Percentage", "Health is restored as a percentage of max health, otherwise to a fixed value");
        addConfig("restoreHealthPercentage", "Restore Health Percentage", "Health is restored to this percentage of max health");
        addConfig("restoreHealth", "Restore Health", "In fixed value mode, restored to this value");
        addConfig("restoreFoodLevel", "Restore Food Level", "Restored to this value");
        addConfig("restoreSaturation", "Restore Saturation", "Restored to this value");

        addConfigGroup("Blub", "Jelly Bunny", "Follower that applies effects while worn and protects the wearer from death");
        addConfig("tickEffectInterval", "Effect Refresh Interval", "Ticks between refreshes, 0 refreshes every tick; keep it well below the effect duration or the effect expires and flickers");
        addConfig("effects", "Effect List", "One entry per line, formatted as effect|duration|amplifier; amplifier 0 is level I, up to 255");
        addConfigGroup("RadiusEffects", "Range Effects", "Effects applied to nearby mobs when death protection triggers");
        addConfig("radius", "Range Radius", "Radius in blocks around the wearer");
        addConfig("affectSelf", "Affect Self", "Whether the wearer itself is affected");
        addConfig("affectPlayers", "Affect Players", "Whether players are affected");
        addConfig("affectPets", "Affect Pets", "Whether tamed pets are affected, meaning any creature with an owner");
        addConfig("affectOtherMobs", "Affect Other Mobs", "Whether other mobs are affected, meaning creatures that are neither the wearer, nor players, nor tamed pets");

        addConfig("entityDataFallback", "Entity Data Fallback", "Where creatures without their own render settings take values from");
        addEntityDataFallbacks("Local Values", "Config Defaults");

        addConfigGroup("MultipleFollowers", "Multiple Followers", "How multiple followers worn by the same wearer are arranged");
        addConfig("followerRenderMode", "Render Mode", "Independent scatters them loosely around the configured position, formation lines them up in a tail where each follows the one ahead");
        addFollowerRenderModes("Independent", "Formation");
        addConfig("deduplicateMode", "Deduplicate Rendering", "How duplicates of the same item worn in several slots are resolved, split by glint keeps one of each glint state while the prefer modes keep only the stated one");
        addDeduplicateModes("Off", "Split by Glint", "Prefer No Glint", "Prefer Glint");
        addConfig("independentFloat", "Independent Float", "Give each follower its own float rhythm and period instead of floating in sync");
        addConfigGroup("IndependentMode", "Independent Mode", "Settings for independent mode, where each follower drifts on its own");
        addConfig("minSpacing", "Min Spacing", "Minimum blocks between followers");
        addConfig("maxSpacing", "Max Spacing", "Maximum blocks between followers");
        addConfig("maxRadius", "Max Radius", "Maximum horizontal distance in blocks a follower may drift from the wearer, independent mode only");
        addConfigGroup("FormationMode", "Formation Mode", "Settings for formation mode, where followers line up in a tail");
        addConfig("formationSpacing", "Formation Spacing", "Blocks between each follower and the one ahead of it");
        addConfig("formationMaxLagDistance", "Formation Max Lag Distance", "Extra blocks a follower may be pulled beyond the formation spacing from the one ahead of it, 0 keeps it within the spacing; to stay out of the wearer's body it may be pushed up to about one wearer radius further");
        addConfig("formationGazeTarget", "Formation Gaze Target", "Where each follower in the tail looks: along the wearer's view, or at the follower ahead of it");
        addFormationGazeTargets("Wearer's View", "Follower Ahead");

        addConfigGroup("FirstPersonDisplay", "First Person Display", "First person display of your own followers");
        addConfig("showInFirstPerson", "Show in First Person", "Show your own followers in first person");

        addConfigGroup("ScaleAndOffset", "Scale and Offset", "Size and position of the follower relative to the wearer's eyes; offsets follow the orbit direction set by spring motion or rotation, not the follower's facing");
        addConfig("renderScale", "Render Scale", "Multiplied by the wearer's own scale");
        addConfig("frontBackOffset", "Front/Back Offset", "Blocks to move forward along the orbit direction, negative moves backward");
        addConfig("leftRightOffset", "Left/Right Offset", "Blocks to move to the right of the orbit direction, negative moves left");
        addConfig("verticalOffset", "Vertical Offset", "Blocks to move up from eye height, negative moves down");
        addConfig("crouchOffset", "Crouch Offset", "Extra blocks to move up while sneaking, negative moves down");

        addConfigGroup("FloatMotion", "Float Motion", "Up and down floating motion");
        addConfig("floatDistance", "Float Distance", "Height in blocks between the highest and lowest point of the float");
        addConfig("floatCycleDuration", "Float Cycle Duration", "Ticks for one full float cycle");

        addConfigGroup("FollowDelay", "Follow Delay", "Trailing behind the wearer while moving");
        addConfig("followSpeed", "Follow Speed", "How quickly the follower catches up, higher values give a shorter trail, 1.0 follows instantly");
        addConfig("maxLagDistance", "Max Lag Distance", "Maximum distance in blocks the follower trails behind, 0 disables trailing");

        addConfigGroup("TurningAndFacing", "Turning and Facing", "How the follower orbits and faces when the wearer turns or looks around");
        addConfig("rotationFollowSpeed", "Rotation Follow Speed", "How quickly the orbit catches up with the head, higher is faster, 1.0 follows instantly");
        addConfig("useShortestArc", "Use Shortest Arc", "Catch up along the shortest arc instead of following every full turn");
        addConfig("orientationFollowSpeed", "Orientation Follow Speed", "How quickly the facing catches up with its target, higher is faster, 1.0 follows instantly");
        addConfig("gazeAtViewTarget", "Gaze at View Target", "Aim the follower worn by a player at that player's own crosshair hit point, using the setting they synced to the server; followers on non-player wearers use this local setting");

        addConfigGroup("SpringMotion", "Spring Motion", "Spring motion for orbiting, overrides rotation when enabled");
        addConfig("springMotionEnabled", "Enable Spring Motion", "Orbit with spring motion that can overshoot and bounce back");
        addConfig("springStiffness", "Spring Stiffness", "Spring stiffness, higher is snappier");
        addConfig("springDamping", "Spring Damping", "Fraction of spring speed kept each tick, lower settles faster");

        addConfig("section.catburger.client.toml", "Client Settings");
        addConfig("section.catburger.client.toml.title", "Cat Burger Client Configuration");
        addConfig("section.catburger.server.toml", "Server Settings");
        addConfig("section.catburger.server.toml.title", "Cat Burger Server Configuration");
    }

    public void Chinese() {
        add(CatBurgerCreativeModeTabs.CATBURGER_TAB_TRANSLATABLE, "猫猫汉堡");
        addItem(CatBurgerItems.FOLLOWER_CONFIG_CARD, "追随者配置卡");
        String cardKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.FOLLOWER_CONFIG_CARD.get());
        add(cardKeyPrefix + "mode", "当前模式：");
        add(cardKeyPrefix + "mode.add", "添加");
        add(cardKeyPrefix + "mode.remove", "移除");
        add(cardKeyPrefix + "mode.wear", "穿戴");
        add(cardKeyPrefix + "hud", "追随者配置卡：");
        add(cardKeyPrefix + "effect.switch_mode", "潜行滚轮：切换模式");
        add(cardKeyPrefix + "effect.pet_sync_notice", "宠物配置同步已开启，配置卡的设置优先于宠物继承");
        add(cardKeyPrefix + "effect.maid_death_protection", "为其他生物穿戴追随者，死亡保护功能不生效");
        add(cardKeyPrefix + "effect.maid_compat", "，东方女仆已主动适配");
        add(cardKeyPrefix + "effect.prefix.add", "添加模式");
        add(cardKeyPrefix + "effect.prefix.remove", "移除模式");
        add(cardKeyPrefix + "effect.prefix.wear", "穿戴模式");
        add(cardKeyPrefix + "effect.add.write", "：右键生物把卡里的设置写入目标");
        add(cardKeyPrefix + "effect.add.copy", "：潜行右键把自己的设置复制进卡");
        add(cardKeyPrefix + "effect.remove.clear", "：右键生物清除目标的设置");
        add(cardKeyPrefix + "effect.remove.reset", "：潜行右键清空卡里的设置");
        add(cardKeyPrefix + "effect.wear.put", "：副手持追随者右键把追随者戴到目标头上");
        add(cardKeyPrefix + "effect.wear.take", "：潜行右键取下目标头上的追随者");
        add(cardKeyPrefix + "applied", "已把配置写入目标");
        add(cardKeyPrefix + "removed", "已清除目标的配置");
        add(cardKeyPrefix + "copied", "已把你的渲染设置复制进配置卡");
        add(cardKeyPrefix + "cleared", "已清空配置卡里的设置");
        add(cardKeyPrefix + "copy_missing", "你还没有可复制的渲染设置");
        add(cardKeyPrefix + "empty", "配置卡里没有设置");
        add(cardKeyPrefix + "reject.player", "不能对玩家使用");
        add(cardKeyPrefix + "reject.owned", "这个生物属于别人");
        add(cardKeyPrefix + "wear.applied", "已把追随者戴到目标头上");
        add(cardKeyPrefix + "wear.removed", "已取下目标头上的追随者");
        add(cardKeyPrefix + "wear.not_follower", "副手需要持有追随者");
        add(cardKeyPrefix + "wear.empty", "目标没有佩戴追随者");
        add(cardKeyPrefix + "wear.locked", "绑定诅咒等效果使目标的头盔位置无法更改");
        addItem(CatBurgerItems.CAT_BURGER, "猫猫汉堡");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "按住[%s]查看详细说明");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "flavor_text", "喵!");
        add(itemKeyPrefix + "effect.food_level_restoration", "佩戴时每%s秒恢复%s点饥饿值，最多恢复至%s点");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "佩戴时每刻恢复%s点饥饿值，最多恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "受到致命伤害时触发死亡保护，生命值恢复至最大生命值的%s%%");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "受到致命伤害时触发死亡保护，生命值恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", "，饥饿值恢复至%s点，饱和度恢复至%s点");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "死亡保护冷却时间：%s秒");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "睡醒后重置死亡保护的冷却时间");

        addItem(CatBurgerItems.BLUB, "果冻兔兔");

        String blubKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.BLUB.get());
        add(blubKeyPrefix + "flavor_text", "布噜~");
        add(blubKeyPrefix + "effect.tick_effects.entry", "佩戴时每%1$s秒获得%2$s（%3$s秒）");
        add(blubKeyPrefix + "effect.tick_effects.entry.every_tick", "佩戴时每刻获得%1$s（%2$s秒）");
        add(blubKeyPrefix + "effect.death_protection.restore_health_percentage", "受到致命伤害时触发死亡保护，生命值恢复至最大生命值的%s%%");
        add(blubKeyPrefix + "effect.death_protection.restore_health", "受到致命伤害时触发死亡保护，生命值恢复至%s点");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_all", "，对%1$s格内的生物施加%2$s");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_excluded", "，对%1$s格内的除%2$s外的生物施加%3$s");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope", "，对%1$s格内的%2$s施加%3$s");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.item", "%1$s（%2$s秒）");
        add(blubKeyPrefix + "effect.list_separator", "、");
        add(blubKeyPrefix + "effect.target.self", "自己");
        add(blubKeyPrefix + "effect.target.players", "玩家");
        add(blubKeyPrefix + "effect.target.pets", "宠物");
        add(blubKeyPrefix + "effect.death_protection.cooldown", "死亡保护冷却时间：%s秒");
        add(blubKeyPrefix + "effect.death_protection.wake_up", "睡醒后重置死亡保护的冷却时间");

        addConfig("title", "猫猫汉堡配置");

        addConfig("petConfigSyncEnabled", "宠物配置同步", "开启后把玩家的渲染配置同步给其宠物，每次配置变化都会遍历所有维度与实体，玩家和生物多时可能卡顿，请慎重开启");
        addConfig("mobActiveWearEnabled", "启用生物主动佩戴", "允许生物主动佩戴追随者");

        addConfigGroup("DeathProtectionCooldown", "死亡保护冷却", "死亡保护冷却，对所有追随者生效");
        addConfig("cooldownDuration", "冷却时长", "死亡保护再次触发所需的刻数，0为无冷却");
        addConfig("resetCooldownOnWakeUp", "睡醒时重置冷却", "佩戴者睡醒后重置冷却");

        addConfigGroup("Followers", "追随者", "每个追随者各自的配置");
        addConfigGroup("CatBurger", "猫猫汉堡", "佩戴时恢复饥饿值并提供死亡保护的追随者");
        addConfigGroup("TickEffects", "Tick效果", "佩戴追随者时反复触发的效果");
        addConfig("foodLevelRestorationInterval", "饥饿值恢复间隔", "两次恢复之间的刻数，0为每刻恢复");
        addConfig("foodLevelRestorationAmount", "饥饿值恢复量", "每次恢复的数值");
        addConfig("foodLevelRestorationLimit", "饥饿值恢复上限", "达到此数值后不再恢复");
        addConfigGroup("DeathProtectionEffect", "死亡保护效果", "佩戴追随者时的死亡保护");
        addConfig("deathProtectionEnabled", "启用死亡保护", "佩戴者受到致命伤害时触发");
        addConfig("useHealthPercentage", "使用生命值百分比", "按最大生命值的百分比恢复生命值，关闭时恢复至固定数值");
        addConfig("restoreHealthPercentage", "恢复生命值百分比", "生命值恢复至最大生命值的此百分比");
        addConfig("restoreHealth", "恢复生命值", "固定数值模式下恢复至此数值");
        addConfig("restoreFoodLevel", "恢复饥饿值", "恢复至此数值");
        addConfig("restoreSaturation", "恢复饱和度", "恢复至此数值");

        addConfigGroup("Blub", "果冻兔兔", "佩戴时施加效果并在死亡时保护佩戴者的追随者");
        addConfig("tickEffectInterval", "效果刷新间隔", "刷新效果的间隔刻数，0为每刻刷新；需明显短于效果时长，否则效果会在到期后重新获得而闪烁");
        addConfig("effects", "效果列表", "每行一条，格式为「效果|持续刻数|等级」，等级按原版计，0为I级，最高255级");
        addConfigGroup("RadiusEffects", "范围效果", "死亡保护触发时对周围生物施加的效果");
        addConfig("radius", "范围半径", "以佩戴者为中心的半径格数");
        addConfig("affectSelf", "影响自己", "佩戴者本人是否受影响");
        addConfig("affectPlayers", "影响玩家", "玩家是否受影响");
        addConfig("affectPets", "影响宠物", "已驯服的宠物（任何有主人的生物）是否受影响");
        addConfig("affectOtherMobs", "影响其他生物", "既非佩戴者本人、也非玩家与宠物的生物是否受影响");

        addConfig("entityDataFallback", "实体数据回退", "没有自身渲染设置的生物从何处取值");
        addEntityDataFallbacks("本地配置值", "配置默认值");

        addConfigGroup("MultipleFollowers", "多件追随者", "同一佩戴者佩戴多件追随者时的排布");
        addConfig("followerRenderMode", "排布方式", "独立让各件围绕配置位置松散散开，编队让它们排成一条尾巴、每件跟随前一件");
        addFollowerRenderModes("独立", "编队");
        addConfig("deduplicateMode", "渲染去重", "同一物品戴在多个槽位时如何取舍：区分附魔光效按有无附魔光效各保留一件，优先档位只保留对应档位的一件");
        addDeduplicateModes("关闭", "区分附魔光效", "优先无附魔光效", "优先有附魔光效");
        addConfig("independentFloat", "独立浮动", "每件追随者使用各自的浮动节奏与周期，而不是同步上下浮动");
        addConfigGroup("IndependentMode", "独立模式", "独立模式的设置，每件追随者各自漂移");
        addConfig("minSpacing", "最小间距", "追随者之间的最小间距，单位为格");
        addConfig("maxSpacing", "最大间距", "追随者之间的最大间距，单位为格");
        addConfig("maxRadius", "活动半径", "追随者与佩戴者之间的最大水平距离，单位为格，仅独立模式生效");
        addConfigGroup("FormationMode", "编队模式", "编队模式的设置，多件追随者排成一条尾巴");
        addConfig("formationSpacing", "编队间距", "每件追随者与它前面一件之间的间距，单位为格");
        addConfig("formationMaxLagDistance", "编队最大滞后距离", "每件追随者相对它前面一件最多比编队间距多拉开的格数，0为不超过编队间距；为避免穿进佩戴者身体，实际距离最多还会再多出约一个佩戴者半径");
        addConfig("formationGazeTarget", "编队注视方向", "编队中每一节看向何处：跟随佩戴者的视线，或看向它前面那一节");
        addFormationGazeTargets("跟随佩戴者视线", "看向前一节");

        addConfigGroup("FirstPersonDisplay", "第一人称显示", "自己跟随者在第一人称下的显示");
        addConfig("showInFirstPerson", "第一人称显示", "在第一人称下显示自己的跟随者");

        addConfigGroup("ScaleAndOffset", "缩放与偏移", "追随者相对佩戴者眼部的大小与位置；偏移沿环绕方向计算，环绕方向由弹簧运动或转向决定，与追随者自身朝向无关");
        addConfig("renderScale", "渲染缩放", "与佩戴者自身缩放相乘");
        addConfig("frontBackOffset", "前后偏移", "沿环绕方向向前偏移的格数，负值向后");
        addConfig("leftRightOffset", "左右偏移", "沿环绕方向向右偏移的格数，负值向左");
        addConfig("verticalOffset", "垂直偏移", "相对眼部高度向上偏移的格数，负值向下");
        addConfig("crouchOffset", "蹲伏偏移", "潜行时额外向上偏移的格数，负值向下");

        addConfigGroup("FloatMotion", "浮动运动", "上下浮动动画");
        addConfig("floatDistance", "浮动距离", "浮动最高点与最低点之间相差的格数");
        addConfig("floatCycleDuration", "浮动周期时长", "完成一次完整浮动所需的刻数");

        addConfigGroup("FollowDelay", "延迟跟随", "佩戴者移动时追随者落后跟随");
        addConfig("followSpeed", "跟随速度", "追随者追上佩戴者的速度，越大拖尾越短，1.0为瞬时跟随");
        addConfig("maxLagDistance", "最大滞后距离", "追随者最多落后的格数，0为不拖尾");

        addConfigGroup("TurningAndFacing", "转向与朝向", "佩戴者转头或环顾时追随者如何环绕和朝向");
        addConfig("rotationFollowSpeed", "转向跟随速度", "环绕追上头部朝向的速度，越大越快，1.0为瞬时跟随");
        addConfig("useShortestArc", "使用最短路径", "沿最短弧线追上头部朝向，不再跟随每一圈完整转身");
        addConfig("orientationFollowSpeed", "朝向跟随速度", "朝向追上目标的速度，越大越快，1.0为瞬时跟随");
        addConfig("gazeAtViewTarget", "注视视线终点", "让玩家佩戴的追随者注视该玩家自己的准心命中点，按其同步过来的设置生效；非玩家佩戴的追随者按本机设置");

        addConfigGroup("SpringMotion", "弹簧运动", "以弹簧运动环绕跟随，启用时覆盖转向");
        addConfig("springMotionEnabled", "启用弹簧运动", "以可能越过并回弹的弹簧运动环绕跟随");
        addConfig("springStiffness", "弹簧刚度", "弹簧刚度，越大回弹越快");
        addConfig("springDamping", "弹簧阻尼", "每刻保留的弹簧速度比例，越小越快静止");

        addConfig("section.catburger.client.toml", "客户端设置");
        addConfig("section.catburger.client.toml.title", "猫猫汉堡客户端配置");
        addConfig("section.catburger.server.toml", "服务端设置");
        addConfig("section.catburger.server.toml.title", "猫猫汉堡服务端配置");
    }

    public void Japanese() {
        add(CatBurgerCreativeModeTabs.CATBURGER_TAB_TRANSLATABLE, "猫バーガー");
        addItem(CatBurgerItems.FOLLOWER_CONFIG_CARD, "フォロワー設定カード");
        String cardKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.FOLLOWER_CONFIG_CARD.get());
        add(cardKeyPrefix + "mode", "現在のモード：");
        add(cardKeyPrefix + "mode.add", "追加");
        add(cardKeyPrefix + "mode.remove", "削除");
        add(cardKeyPrefix + "mode.wear", "装着");
        add(cardKeyPrefix + "hud", "フォロワー設定カード：");
        add(cardKeyPrefix + "effect.switch_mode", "スニークスクロール：モードを切り替える");
        add(cardKeyPrefix + "effect.pet_sync_notice", "ペット設定同期が有効なため、カードの設定はペットの継承より優先される");
        add(cardKeyPrefix + "effect.maid_death_protection", "他の生物にフォロワーを装着すると死亡保護は適用されない");
        add(cardKeyPrefix + "effect.maid_compat", "（東方メイドは既に対応済み）");
        add(cardKeyPrefix + "effect.prefix.add", "追加モード");
        add(cardKeyPrefix + "effect.prefix.remove", "削除モード");
        add(cardKeyPrefix + "effect.prefix.wear", "装着モード");
        add(cardKeyPrefix + "effect.add.write", "：生物を右クリックしてカードの設定を対象に書き込む");
        add(cardKeyPrefix + "effect.add.copy", "：スニーク右クリックで自分の設定をカードにコピーする");
        add(cardKeyPrefix + "effect.remove.clear", "：生物を右クリックして対象の設定を消去する");
        add(cardKeyPrefix + "effect.remove.reset", "：スニーク右クリックでカードの設定を空にする");
        add(cardKeyPrefix + "effect.wear.put", "：オフハンドにフォロワーを持って右クリックすると対象の頭に付ける");
        add(cardKeyPrefix + "effect.wear.take", "：スニーク右クリックで対象の頭からフォロワーを外す");
        add(cardKeyPrefix + "applied", "設定を対象に書き込みました");
        add(cardKeyPrefix + "removed", "対象の設定を消去しました");
        add(cardKeyPrefix + "copied", "レンダリング設定をカードにコピーしました");
        add(cardKeyPrefix + "cleared", "カードに保存された設定を消去しました");
        add(cardKeyPrefix + "copy_missing", "コピーできるレンダリング設定がありません");
        add(cardKeyPrefix + "empty", "カードに設定が保存されていません");
        add(cardKeyPrefix + "reject.player", "プレイヤーには使用できません");
        add(cardKeyPrefix + "reject.owned", "この生物は他の人のものです");
        add(cardKeyPrefix + "wear.applied", "フォロワーを対象の頭に付けました");
        add(cardKeyPrefix + "wear.removed", "対象の頭からフォロワーを外しました");
        add(cardKeyPrefix + "wear.not_follower", "オフハンドにフォロワーを持つ必要があります");
        add(cardKeyPrefix + "wear.empty", "対象はフォロワーを装着していません");
        add(cardKeyPrefix + "wear.locked", "呪いなどの効果により対象の頭装備を変更できません");
        addItem(CatBurgerItems.CAT_BURGER, "猫バーガー");

        add(TooltipUtil.TOOLTIP_PREFIX + "shift_hint", "[%s]を押して詳細を表示");
        add(TooltipUtil.TOOLTIP_PREFIX + "hint.shift", "Shift");

        String itemKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.CAT_BURGER.get());
        add(itemKeyPrefix + "flavor_text", "にゃー!");
        add(itemKeyPrefix + "effect.food_level_restoration", "装備中、%s秒ごとに満腹度を%s回復し、最大%sまで回復する");
        add(itemKeyPrefix + "effect.food_level_restoration.every_tick", "装備中、毎tick満腹度を%s回復し、最大%sまで回復する");
        add(itemKeyPrefix + "effect.death_protection.restore_health_percentage", "致死ダメージを受けると死亡保護が発動し、体力が最大体力の%s%%まで回復");
        add(itemKeyPrefix + "effect.death_protection.restore_health", "致死ダメージを受けると死亡保護が発動し、体力が%sまで回復");
        add(itemKeyPrefix + "effect.death_protection.restore_food_level", "、満腹度が%s、隠し満腹度が%sまで回復");
        add(itemKeyPrefix + "effect.death_protection.cooldown", "死亡保護のクールダウン：%s秒");
        add(itemKeyPrefix + "effect.death_protection.wake_up", "起床すると死亡保護のクールダウンがリセットされる");

        addItem(CatBurgerItems.BLUB, "ジェリーバニー");

        String blubKeyPrefix = TooltipUtil.itemKeyPrefix(CatBurgerItems.BLUB.get());
        add(blubKeyPrefix + "flavor_text", "ぷるん");
        add(blubKeyPrefix + "effect.tick_effects.entry", "装備中、%1$s秒ごとに%2$s（%3$s秒間）を付与する");
        add(blubKeyPrefix + "effect.tick_effects.entry.every_tick", "装備中、毎tick %1$s（%2$s秒間）を付与する");
        add(blubKeyPrefix + "effect.death_protection.restore_health_percentage", "致死ダメージを受けると死亡保護が発動し、体力が最大体力の%s%%まで回復");
        add(blubKeyPrefix + "effect.death_protection.restore_health", "致死ダメージを受けると死亡保護が発動し、体力が%sまで回復");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_all", "、%1$sブロック以内のMobに%2$sを付与する");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope_excluded", "、%1$sブロック以内の%2$s以外のMobに%3$sを付与する");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.scope", "、%1$sブロック以内の%2$sに%3$sを付与する");
        add(blubKeyPrefix + "effect.death_protection.radius_effects.item", "%1$s（%2$s秒間）");
        add(blubKeyPrefix + "effect.list_separator", "、");
        add(blubKeyPrefix + "effect.target.self", "自分");
        add(blubKeyPrefix + "effect.target.players", "プレイヤー");
        add(blubKeyPrefix + "effect.target.pets", "手懐けたペット");
        add(blubKeyPrefix + "effect.death_protection.cooldown", "死亡保護のクールダウン：%s秒");
        add(blubKeyPrefix + "effect.death_protection.wake_up", "起床すると死亡保護のクールダウンがリセットされる");

        addConfig("title", "猫バーガーの設定");

        addConfig("petConfigSyncEnabled", "ペット設定同期", "有効にするとプレイヤーのレンダリング設定がペットにコピーされる。設定変更のたびに全ディメンションとエンティティを走査するため、プレイヤーやMobが多いとラグの原因になる。慎重に有効化すること");
        addConfig("mobActiveWearEnabled", "Mobの自発的な装着を有効化", "Mobがフォロワーを自ら装備できるようにする");

        addConfigGroup("DeathProtectionCooldown", "死亡保護のクールダウン", "死亡保護のクールダウン、全てのフォロワーに適用");
        addConfig("cooldownDuration", "クールダウン時間", "死亡保護が再び発動するまでのtick数、0でクールダウンなし");
        addConfig("resetCooldownOnWakeUp", "起床時にクールダウンをリセット", "装備者が起床するとクールダウンがリセットされる");

        addConfigGroup("Followers", "フォロワー", "フォロワーごとの設定");
        addConfigGroup("CatBurger", "猫バーガー", "装備中は満腹度を回復し死亡保護を行うフォロワー");
        addConfigGroup("TickEffects", "Tick効果", "フォロワー装備中に繰り返し発動する効果");
        addConfig("foodLevelRestorationInterval", "満腹度回復の間隔", "回復の間隔となるtick数、0で毎tick回復");
        addConfig("foodLevelRestorationAmount", "満腹度回復量", "1回ごとに回復する量");
        addConfig("foodLevelRestorationLimit", "満腹度回復の上限", "この値に達すると回復しない");
        addConfigGroup("DeathProtectionEffect", "死亡保護効果", "フォロワー装備中の死亡保護");
        addConfig("deathProtectionEnabled", "死亡保護を有効化", "装備者が致死ダメージを受けると発動する");
        addConfig("useHealthPercentage", "体力割合を使用", "最大体力に対する割合で回復し、無効時は固定値まで回復");
        addConfig("restoreHealthPercentage", "回復体力の割合", "体力が最大体力のこの割合まで回復する");
        addConfig("restoreHealth", "体力を回復", "固定値モードではこの値まで回復する");
        addConfig("restoreFoodLevel", "満腹度を回復", "この値まで回復する");
        addConfig("restoreSaturation", "隠し満腹度を回復", "この値まで回復する");

        addConfigGroup("Blub", "ジェリーバニー", "装備中は効果を付与し、死亡時に装備者を保護するフォロワー");
        addConfig("tickEffectInterval", "効果の更新間隔", "効果を更新する間隔のtick数、0で毎tick更新；効果時間より十分短くしないと効果が切れてちらつく");
        addConfig("effects", "効果リスト", "1行に1件、「効果|効果時間|レベル」の形式；レベルは0がI、最大255");
        addConfigGroup("RadiusEffects", "範囲効果", "死亡保護の発動時に周囲のMobへ付与する効果");
        addConfig("radius", "効果範囲の半径", "装備者を中心とした半径のブロック数");
        addConfig("affectSelf", "自分に影響", "装備者自身が影響を受けるかどうか");
        addConfig("affectPlayers", "プレイヤーに影響", "プレイヤーが影響を受けるかどうか");
        addConfig("affectPets", "ペットに影響", "手懐けたペット（飼い主のいるMob）が影響を受けるかどうか");
        addConfig("affectOtherMobs", "その他のMobに影響", "装備者でもプレイヤーでも手懐けたペットでもないMobが影響を受けるかどうか");

        addConfig("entityDataFallback", "エンティティデータのフォールバック", "自身のレンダリング設定を持たない生物がどこから値を取るか");
        addEntityDataFallbacks("ローカル値", "設定デフォルト");

        addConfigGroup("MultipleFollowers", "複数のフォロワー", "同じ装備者が複数のフォロワーを装備した時の並べ方");
        addConfig("followerRenderMode", "配置方式", "独立は設定位置の周りに緩く散らばり、編隊は尻尾のように一列に並んで前のフォロワーを追う");
        addFollowerRenderModes("独立", "編隊");
        addConfig("deduplicateMode", "描画の重複排除", "同じアイテムを複数のスロットに装備した時の扱い：オーラで区別するとオーラの有無ごとに1つずつ描画し、優先設定では指定した側だけを描画する");
        addDeduplicateModes("オフ", "オーラで区別", "オーラなしを優先", "オーラありを優先");
        addConfig("independentFloat", "個別の浮遊", "フォロワーごとに浮遊のリズムと周期を変え、同期して浮遊しないようにする");
        addConfigGroup("IndependentMode", "独立モード", "独立モードの設定、各フォロワーが個別に漂う");
        addConfig("minSpacing", "最小間隔", "フォロワー同士の最小間隔、ブロック数");
        addConfig("maxSpacing", "最大間隔", "フォロワー同士の最大間隔、ブロック数");
        addConfig("maxRadius", "活動半径", "フォロワーと装着者との最大水平距離、ブロック数、独立モードのみ有効");
        addConfigGroup("FormationMode", "編隊モード", "編隊モードの設定、複数のフォロワーが尻尾のように一列に並ぶ");
        addConfig("formationSpacing", "編隊の間隔", "各フォロワーと前のフォロワーとの間隔、ブロック数");
        addConfig("formationMaxLagDistance", "編隊の最大遅延距離", "各フォロワーが前のフォロワーから編隊の間隔を超えて引き離される最大のブロック数、0で間隔を超えない；装備者の体にめり込まないよう、さらに装備者の半径ほど押し出されることがある");
        addConfig("formationGazeTarget", "編隊の注視方向", "編隊の各節がどこを見るか：装備者の視線に従う、または前の節を見る");
        addFormationGazeTargets("装備者の視線に従う", "前の節を見る");

        addConfigGroup("FirstPersonDisplay", "一人称表示", "自分のフォロワーの一人称での表示");
        addConfig("showInFirstPerson", "一人称で表示", "一人称で自分のフォロワーを表示する");

        addConfigGroup("ScaleAndOffset", "スケールとオフセット", "装備者の目の位置を基準としたフォロワーの大きさと位置；オフセットはバネ運動または回転で決まる周回方向に沿い、フォロワー自身の向きとは無関係");
        addConfig("renderScale", "描画倍率", "装備者自身のスケールに乗算される");
        addConfig("frontBackOffset", "前後オフセット", "周回方向へ前に動かすブロック数、負の値で後ろ");
        addConfig("leftRightOffset", "左右オフセット", "周回方向の右に動かすブロック数、負の値で左");
        addConfig("verticalOffset", "垂直オフセット", "目の高さから上に動かすブロック数、負の値で下");
        addConfig("crouchOffset", "しゃがみ時オフセット", "スニーク中に追加で上に動かすブロック数、負の値で下");

        addConfigGroup("FloatMotion", "浮遊運動", "上下に揺れる浮遊アニメーション");
        addConfig("floatDistance", "浮遊距離", "浮遊の最高点と最低点の差のブロック数");
        addConfig("floatCycleDuration", "浮遊周期時間", "浮遊が1周するのにかかるtick数");

        addConfigGroup("FollowDelay", "追従遅延", "装備者の移動時にフォロワーが遅れて追従する");
        addConfig("followSpeed", "追従速度", "フォロワーが追いつく速さ、大きいほど遅れが短い、1.0で即座に追従");
        addConfig("maxLagDistance", "最大遅延距離", "フォロワーが遅れる最大のブロック数、0で遅延なし");

        addConfigGroup("TurningAndFacing", "回転と向き", "装備者が振り向いたり見回したりした時のフォロワーの周回と向き");
        addConfig("rotationFollowSpeed", "回転の追従速度", "周回が頭の向きに追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("useShortestArc", "最短経路を使用", "1周ごとに追従せず、最短の弧で頭の向きに追いつく");
        addConfig("orientationFollowSpeed", "向きの追従速度", "向きが目標に追いつく速さ、大きいほど速い、1.0で即座に追従");
        addConfig("gazeAtViewTarget", "視線の終点を注視", "プレイヤーが装着しているフォロワーを、そのプレイヤー自身の照準点に向けさせる（同期された設定に従う）。非プレイヤーの装着者はローカル設定に従う");

        addConfigGroup("SpringMotion", "バネ運動", "バネ運動で周回する、有効時は回転より優先");
        addConfig("springMotionEnabled", "バネ運動を有効化", "行き過ぎて跳ね返るバネ運動で周回する");
        addConfig("springStiffness", "バネの硬さ", "バネの硬さ、大きいほど素早く戻る");
        addConfig("springDamping", "バネの減衰", "毎tick保持されるバネの速度の割合、小さいほど早く静止");

        addConfig("section.catburger.client.toml", "クライアント設定");
        addConfig("section.catburger.client.toml.title", "猫バーガーのクライアント設定");
        addConfig("section.catburger.server.toml", "サーバー設定");
        addConfig("section.catburger.server.toml.title", "猫バーガーのサーバー設定");
    }

    /**
     * 为排布方式枚举的每个常量写入显示名，穷尽 switch 使新增常量时编译失败以强制补译名。
     */
    private void addFollowerRenderModes(String independent, String formation) {
        for (FollowerRenderMode renderMode : FollowerRenderMode.values()) {
            add(renderMode.getTranslationKey(), switch (renderMode) {
                case INDEPENDENT -> independent;
                case FORMATION -> formation;
            });
        }
    }

    /**
     * 为编队注视方向枚举的每个常量写入显示名，穷尽 switch 使新增常量时编译失败以强制补译名。
     */
    private void addFormationGazeTargets(String wearerView, String frontFollower) {
        for (FormationGazeTarget gazeTarget : FormationGazeTarget.values()) {
            add(gazeTarget.getTranslationKey(), switch (gazeTarget) {
                case WEARER_VIEW -> wearerView;
                case FRONT_FOLLOWER -> frontFollower;
            });
        }
    }

    /**
     * 为回退来源枚举的每个常量写入显示名，穷尽 switch 使新增常量时编译失败以强制补译名。
     */
    private void addEntityDataFallbacks(String local, String configDefault) {
        for (EntityDataFallback fallback : EntityDataFallback.values()) {
            add(fallback.getTranslationKey(), switch (fallback) {
                case LOCAL -> local;
                case CONFIG_DEFAULT -> configDefault;
            });
        }
    }

    /**
     * 为去重档位枚举的每个常量写入显示名，穷尽 switch 使新增常量时编译失败以强制补译名。
     */
    private void addDeduplicateModes(String off, String splitByGlint, String preferNoGlint, String preferGlint) {
        for (DeduplicateMode deduplicateMode : DeduplicateMode.values()) {
            add(deduplicateMode.getTranslationKey(), switch (deduplicateMode) {
                case OFF -> off;
                case SPLIT_BY_GLINT -> splitByGlint;
                case PREFER_NO_GLINT -> preferNoGlint;
                case PREFER_GLINT -> preferGlint;
            });
        }
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

    /**
     * 为可进入的分组写入名称、说明与按钮文字，按钮文字与分组名一致。
     */
    private void addConfigGroup(String key, String name, @Nullable String tooltip) {
        addConfig(key, name, tooltip);
        addConfig(key + ".button", name);
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
