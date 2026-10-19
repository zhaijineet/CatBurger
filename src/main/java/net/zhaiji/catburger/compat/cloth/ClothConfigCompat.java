package net.zhaiji.catburger.compat.cloth;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.config.CatBurgerClientConfig;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.config.ConfiguredEffect;
import net.zhaiji.catburger.config.FollowerRenderMode;
import net.zhaiji.catburger.config.FormationGazeTarget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cloth Config 配置界面：按类型分别读取本模组的配置值，保存时写回并落盘；
 * 分组层级与1.21.1的push/pop结构一一对应，世界配置只在服务端配置已加载时才可编辑。
 */
public class ClothConfigCompat {
    private static final String KEY_PREFIX = CatBurger.MOD_ID + ".configuration.";

    public static void registerConfigScreen() {
        // 直接挂到本模组容器上，不依赖 FMLClientSetupEvent 期间 ModLoadingContext 的活动容器状态
        ModList.get().getModContainerById(CatBurger.MOD_ID).ifPresent(modContainer ->
                modContainer.registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(ClothConfigCompat::createConfigScreen)
                )
        );
    }

    private static Screen createConfigScreen(Minecraft minecraft, Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(Component.translatable(KEY_PREFIX + "title"))
                                             .setSavingRunnable(ClothConfigCompat::saveAllLoadedConfigs);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory clientCategory = builder.getOrCreateCategory(getCategoryTitle(ModConfig.Type.CLIENT));
        buildEntries(clientCategory, entryBuilder, ModConfig.Type.CLIENT);

        ConfigCategory serverCategory = builder.getOrCreateCategory(getCategoryTitle(ModConfig.Type.SERVER));
        buildEntries(serverCategory, entryBuilder, ModConfig.Type.SERVER);

        return builder.build();
    }

    /**
     * 取本模组指定类型的配置，尚未注册（如专用服端的客户端配置）时返回 null。
     * 是否可编辑不在这里判断，交给 {@link #getLockReason} 按三重条件决定。
     */
    @Nullable
    private static ModConfig getModConfigs(ModConfig.Type type) {
        Set<ModConfig> modConfigs = ConfigTracker.INSTANCE.configSets().get(type);
        if (modConfigs == null) return null;

        // configSets里放的是synchronizedSet，遍历必须持有同一把锁
        synchronized (modConfigs) {
            for (ModConfig modConfig : modConfigs) {
                if (CatBurger.MOD_ID.equals(modConfig.getModId())) return modConfig;
            }
        }

        return null;
    }

    /**
     * 判断配置是否已经加载完成，与 ForgeConfigSpec 内部给 ConfigValue.get 用的判据一致；
     * 未加载时读取配置值在开发环境会直接抛异常，所以界面必须在这一步拦住。
     */
    private static boolean isLoaded(ModConfig modConfig) {
        IConfigSpec<?> configSpec = modConfig.getSpec();
        return configSpec instanceof ForgeConfigSpec forgeConfigSpec && forgeConfigSpec.isLoaded();
    }

    private static Component getCategoryTitle(ModConfig.Type type) {
        String sectionKey = type == ModConfig.Type.CLIENT ? "section.client" : "section.server";
        return Component.translatable(KEY_PREFIX + sectionKey);
    }

    /**
     * 三重锁定，优先级与1.21.1一致：配置未加载 > 联机 > 开放局域网，命中第一条即返回，全部不命中返回null。
     * 锁定时只显示原因文本，不构建任何条目，也不读不写对应 spec。
     */
    @Nullable
    private static Component getLockReason(ModConfig.Type type, @Nullable ModConfig modConfig) {
        if (modConfig == null || !isLoaded(modConfig)) {
            String notLoadedKey = type == ModConfig.Type.SERVER ? "server_config_not_loaded" : "client_config_not_loaded";
            return Component.translatable(KEY_PREFIX + notLoadedKey);
        }

        if (type == ModConfig.Type.SERVER) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getCurrentServer() != null && !minecraft.isSingleplayer()) {
                return Component.translatable(KEY_PREFIX + "server_config_while_online");
            }
            if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer().isPublished()) {
                return Component.translatable(KEY_PREFIX + "server_config_while_lan");
            }
        }

        return null;
    }

    /**
     * 保存所有已加载的本模组配置并投递重载事件，未加载的配置不碰（未加载时落盘会读到空配置数据）。
     * ModConfig.save只写文件不触发事件，不补这一步渲染参数、上传数据与tooltip缓存都不会刷新；
     * 事件必须经容器投递到mod bus，监听方注册在那里，投到其他总线既不到达也不报错。
     */
    private static void saveAllLoadedConfigs() {
        for (ModConfig.Type type : List.of(ModConfig.Type.CLIENT, ModConfig.Type.SERVER)) {
            ModConfig modConfig = getModConfigs(type);
            if (modConfig == null || !isLoaded(modConfig)) continue;
            // 联机或连原版服务器时，服务端配置数据来自同步或默认生成，不是文件配置，落盘会因强转文件配置而失败
            if (!(modConfig.getConfigData() instanceof CommentedFileConfig)) continue;

            modConfig.save();
            ModList.get().getModContainerById(CatBurger.MOD_ID)
                   .ifPresent(modContainer -> modContainer.dispatchConfigEvent(IConfigEvent.reloading(modConfig)));
        }
    }

    private static void buildEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig.Type type) {
        Component lockReason = getLockReason(type, getModConfigs(type));
        if (lockReason != null) {
            category.addEntry(entryBuilder.startTextDescription(lockReason).build());
            return;
        }

        if (type == ModConfig.Type.CLIENT) buildClientEntries(category, entryBuilder);
        if (type == ModConfig.Type.SERVER) buildServerEntries(category, entryBuilder);
    }

    private static void buildClientEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder independentMode = subCategory(entryBuilder, "IndependentMode",
                doubleEntry(entryBuilder, "minSpacing", CatBurgerClientConfig.MIN_SPACING, CatBurgerClientConfig.MIN_SPACING_LOWER, CatBurgerClientConfig.MIN_SPACING_UPPER),
                doubleEntry(entryBuilder, "maxSpacing", CatBurgerClientConfig.MAX_SPACING, CatBurgerClientConfig.MAX_SPACING_LOWER, CatBurgerClientConfig.MAX_SPACING_UPPER),
                doubleEntry(entryBuilder, "maxRadius", CatBurgerClientConfig.MAX_RADIUS, CatBurgerClientConfig.MAX_RADIUS_LOWER, CatBurgerClientConfig.MAX_RADIUS_UPPER)
        );

        SubCategoryBuilder formationMode = subCategory(entryBuilder, "FormationMode",
                doubleEntry(entryBuilder, "formationSpacing", CatBurgerClientConfig.FORMATION_SPACING, CatBurgerClientConfig.FORMATION_SPACING_LOWER, CatBurgerClientConfig.FORMATION_SPACING_UPPER),
                doubleEntry(entryBuilder, "formationMaxLagDistance", CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE, CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE_LOWER, CatBurgerClientConfig.FORMATION_MAX_LAG_DISTANCE_UPPER),
                entryBuilder.startEnumSelector(entryName("formationGazeTarget"), FormationGazeTarget.class, CatBurgerClientConfig.FORMATION_GAZE_TARGET.get())
                             .setDefaultValue(CatBurgerClientConfig.FORMATION_GAZE_TARGET.getDefault())
                             .setEnumNameProvider(gazeTarget -> Component.translatable(((FormationGazeTarget) gazeTarget).getTranslationKey()))
                             .setTooltip(entryTooltip("formationGazeTarget"))
                             .setSaveConsumer(CatBurgerClientConfig.FORMATION_GAZE_TARGET::set)
                             .build()
        );

        category.addEntry(subCategory(entryBuilder, "MultipleFollowers",
                entryBuilder.startEnumSelector(entryName("followerRenderMode"), FollowerRenderMode.class, CatBurgerClientConfig.FOLLOWER_RENDER_MODE.get())
                             .setDefaultValue(CatBurgerClientConfig.FOLLOWER_RENDER_MODE.getDefault())
                             .setEnumNameProvider(renderMode -> Component.translatable(((FollowerRenderMode) renderMode).getTranslationKey()))
                             .setTooltip(entryTooltip("followerRenderMode"))
                             .setSaveConsumer(CatBurgerClientConfig.FOLLOWER_RENDER_MODE::set)
                             .build(),
                booleanEntry(entryBuilder, "deduplicateRendering", CatBurgerClientConfig.DEDUPLICATE_RENDERING),
                booleanEntry(entryBuilder, "independentFloat", CatBurgerClientConfig.INDEPENDENT_FLOAT),
                independentMode.build(),
                formationMode.build()
        ).build());

        category.addEntry(subCategory(entryBuilder, "FirstPersonDisplay",
                booleanEntry(entryBuilder, "showInFirstPerson", CatBurgerClientConfig.SHOW_IN_FIRST_PERSON)
        ).build());

        category.addEntry(subCategory(entryBuilder, "ScaleAndOffset",
                doubleEntry(entryBuilder, "renderScale", CatBurgerClientConfig.RENDER_SCALE, CatBurgerClientConfig.RENDER_SCALE_LOWER, CatBurgerClientConfig.RENDER_SCALE_UPPER),
                doubleEntry(entryBuilder, "frontBackOffset", CatBurgerClientConfig.FRONT_BACK_OFFSET, CatBurgerClientConfig.FRONT_BACK_OFFSET_LOWER, CatBurgerClientConfig.FRONT_BACK_OFFSET_UPPER),
                doubleEntry(entryBuilder, "leftRightOffset", CatBurgerClientConfig.LEFT_RIGHT_OFFSET, CatBurgerClientConfig.LEFT_RIGHT_OFFSET_LOWER, CatBurgerClientConfig.LEFT_RIGHT_OFFSET_UPPER),
                doubleEntry(entryBuilder, "verticalOffset", CatBurgerClientConfig.VERTICAL_OFFSET, CatBurgerClientConfig.VERTICAL_OFFSET_LOWER, CatBurgerClientConfig.VERTICAL_OFFSET_UPPER),
                doubleEntry(entryBuilder, "crouchOffset", CatBurgerClientConfig.CROUCH_OFFSET, CatBurgerClientConfig.CROUCH_OFFSET_LOWER, CatBurgerClientConfig.CROUCH_OFFSET_UPPER)
        ).build());

        category.addEntry(subCategory(entryBuilder, "FloatMotion",
                doubleEntry(entryBuilder, "floatDistance", CatBurgerClientConfig.FLOAT_DISTANCE, CatBurgerClientConfig.FLOAT_DISTANCE_LOWER, CatBurgerClientConfig.FLOAT_DISTANCE_UPPER),
                doubleEntry(entryBuilder, "floatCycleDuration", CatBurgerClientConfig.FLOAT_CYCLE_DURATION, CatBurgerClientConfig.FLOAT_CYCLE_DURATION_LOWER, CatBurgerClientConfig.FLOAT_CYCLE_DURATION_UPPER)
        ).build());

        category.addEntry(subCategory(entryBuilder, "FollowDelay",
                doubleEntry(entryBuilder, "followSpeed", CatBurgerClientConfig.FOLLOW_SPEED, CatBurgerClientConfig.FOLLOW_SPEED_LOWER, CatBurgerClientConfig.FOLLOW_SPEED_UPPER),
                doubleEntry(entryBuilder, "maxLagDistance", CatBurgerClientConfig.MAX_LAG_DISTANCE, CatBurgerClientConfig.MAX_LAG_DISTANCE_LOWER, CatBurgerClientConfig.MAX_LAG_DISTANCE_UPPER)
        ).build());

        SubCategoryBuilder springMotion = subCategory(entryBuilder, "SpringMotion",
                booleanEntry(entryBuilder, "springMotionEnabled", CatBurgerClientConfig.SPRING_MOTION_ENABLED),
                doubleEntry(entryBuilder, "springStiffness", CatBurgerClientConfig.SPRING_STIFFNESS, CatBurgerClientConfig.SPRING_STIFFNESS_LOWER, CatBurgerClientConfig.SPRING_STIFFNESS_UPPER),
                doubleEntry(entryBuilder, "springDamping", CatBurgerClientConfig.SPRING_DAMPING, CatBurgerClientConfig.SPRING_DAMPING_LOWER, CatBurgerClientConfig.SPRING_DAMPING_UPPER)
        );

        category.addEntry(subCategory(entryBuilder, "TurningAndFacing",
                doubleEntry(entryBuilder, "rotationFollowSpeed", CatBurgerClientConfig.ROTATION_FOLLOW_SPEED, CatBurgerClientConfig.ROTATION_FOLLOW_SPEED_LOWER, CatBurgerClientConfig.ROTATION_FOLLOW_SPEED_UPPER),
                booleanEntry(entryBuilder, "useShortestArc", CatBurgerClientConfig.USE_SHORTEST_ARC),
                doubleEntry(entryBuilder, "orientationFollowSpeed", CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED, CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED_LOWER, CatBurgerClientConfig.ORIENTATION_FOLLOW_SPEED_UPPER),
                booleanEntry(entryBuilder, "gazeAtViewTarget", CatBurgerClientConfig.GAZE_AT_VIEW_TARGET),
                springMotion.build()
        ).build());
    }

    private static void buildServerEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        category.addEntry(subCategory(entryBuilder, "DeathProtectionCooldown",
                intEntry(entryBuilder, "cooldownDuration", CatBurgerServerConfig.COOLDOWN_DURATION, 0, Integer.MAX_VALUE),
                booleanEntry(entryBuilder, "resetCooldownOnWakeUp", CatBurgerServerConfig.RESET_COOLDOWN_ON_WAKE_UP)
        ).build());

        SubCategoryBuilder catBurgerTickEffects = subCategory(entryBuilder, "TickEffects",
                intEntry(entryBuilder, "foodLevelRestorationInterval", CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_INTERVAL, 0, Integer.MAX_VALUE),
                intEntry(entryBuilder, "foodLevelRestorationAmount", CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_AMOUNT, 0, Integer.MAX_VALUE),
                intEntry(entryBuilder, "foodLevelRestorationLimit", CatBurgerServerConfig.FOOD_LEVEL_RESTORATION_LIMIT, 0, Integer.MAX_VALUE)
        );

        SubCategoryBuilder catBurgerDeathProtection = subCategory(entryBuilder, "DeathProtectionEffect",
                booleanEntry(entryBuilder, "deathProtectionEnabled", CatBurgerServerConfig.DEATH_PROTECTION_ENABLED),
                booleanEntry(entryBuilder, "useHealthPercentage", CatBurgerServerConfig.USE_HEALTH_PERCENTAGE),
                doubleEntry(entryBuilder, "restoreHealthPercentage", CatBurgerServerConfig.RESTORE_HEALTH_PERCENTAGE, 0.0, 100.0),
                intEntry(entryBuilder, "restoreHealth", CatBurgerServerConfig.RESTORE_HEALTH, 0, Integer.MAX_VALUE),
                intEntry(entryBuilder, "restoreFoodLevel", CatBurgerServerConfig.RESTORE_FOOD_LEVEL, 0, Integer.MAX_VALUE),
                intEntry(entryBuilder, "restoreSaturation", CatBurgerServerConfig.RESTORE_SATURATION, 0, Integer.MAX_VALUE)
        );

        SubCategoryBuilder catBurger = subCategory(entryBuilder, "CatBurger",
                catBurgerTickEffects.build(),
                catBurgerDeathProtection.build()
        );

        SubCategoryBuilder blubTickEffects = subCategory(entryBuilder, "TickEffects",
                intEntry(entryBuilder, "tickEffectInterval", CatBurgerServerConfig.BLUB_TICK_EFFECT_INTERVAL, 0, Integer.MAX_VALUE),
                stringListEntry(entryBuilder, "effects", CatBurgerServerConfig.BLUB_TICK_EFFECTS)
        );

        SubCategoryBuilder blubRadiusEffects = subCategory(entryBuilder, "RadiusEffects",
                doubleEntry(entryBuilder, "radius", CatBurgerServerConfig.BLUB_RADIUS, 0.0, 64.0),
                booleanEntry(entryBuilder, "affectSelf", CatBurgerServerConfig.BLUB_RADIUS_AFFECT_SELF),
                booleanEntry(entryBuilder, "affectPlayers", CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PLAYERS),
                booleanEntry(entryBuilder, "affectPets", CatBurgerServerConfig.BLUB_RADIUS_AFFECT_PETS),
                booleanEntry(entryBuilder, "affectOtherMobs", CatBurgerServerConfig.BLUB_RADIUS_AFFECT_OTHER_MOBS),
                stringListEntry(entryBuilder, "effects", CatBurgerServerConfig.BLUB_RADIUS_EFFECTS)
        );

        SubCategoryBuilder blubDeathProtection = subCategory(entryBuilder, "DeathProtectionEffect",
                booleanEntry(entryBuilder, "blubDeathProtectionEnabled", CatBurgerServerConfig.BLUB_DEATH_PROTECTION_ENABLED),
                booleanEntry(entryBuilder, "blubUseHealthPercentage", CatBurgerServerConfig.BLUB_USE_HEALTH_PERCENTAGE),
                doubleEntry(entryBuilder, "blubRestoreHealthPercentage", CatBurgerServerConfig.BLUB_RESTORE_HEALTH_PERCENTAGE, 0.0, 100.0),
                intEntry(entryBuilder, "blubRestoreHealth", CatBurgerServerConfig.BLUB_RESTORE_HEALTH, 0, Integer.MAX_VALUE),
                blubRadiusEffects.build()
        );

        SubCategoryBuilder blub = subCategory(entryBuilder, "Blub",
                blubTickEffects.build(),
                blubDeathProtection.build()
        );

        category.addEntry(subCategory(entryBuilder, "Followers",
                catBurger.build(),
                blub.build()
        ).build());

        category.addEntry(subCategory(entryBuilder, "MobActiveWear",
                booleanEntry(entryBuilder, "mobActiveWearEnabled", CatBurgerServerConfig.MOB_ACTIVE_WEAR_ENABLED)
        ).build());
    }

    /**
     * 建一个默认收起的分组，条目按传入顺序加入，层级与配置的push/pop结构一致。
     */
    private static SubCategoryBuilder subCategory(ConfigEntryBuilder entryBuilder, String key, AbstractConfigListEntry<?>... entries) {
        SubCategoryBuilder subCategory = entryBuilder.startSubCategory(groupName(key)).setExpanded(false);
        for (AbstractConfigListEntry<?> entry : entries) subCategory.add(entry);
        return subCategory;
    }

    private static Component groupName(String key) {
        return Component.translatable(KEY_PREFIX + key);
    }

    private static Component entryName(String key) {
        return Component.translatable(KEY_PREFIX + key);
    }

    private static Component[] entryTooltip(String key) {
        return new Component[]{Component.translatable(KEY_PREFIX + key + ".tooltip")};
    }

    private static AbstractConfigListEntry<?> doubleEntry(ConfigEntryBuilder entryBuilder, String key, ForgeConfigSpec.DoubleValue value, double lowerBound, double upperBound) {
        return entryBuilder.startDoubleField(entryName(key), value.get())
                           .setDefaultValue(value.getDefault())
                           .setMin(lowerBound)
                           .setMax(upperBound)
                           .setTooltip(entryTooltip(key))
                           .setSaveConsumer(value::set)
                           .build();
    }

    private static AbstractConfigListEntry<?> intEntry(ConfigEntryBuilder entryBuilder, String key, ForgeConfigSpec.IntValue value, int lowerBound, int upperBound) {
        return entryBuilder.startIntField(entryName(key), value.get())
                           .setDefaultValue(value.getDefault())
                           .setMin(lowerBound)
                           .setMax(upperBound)
                           .setTooltip(entryTooltip(key))
                           .setSaveConsumer(value::set)
                           .build();
    }

    private static AbstractConfigListEntry<?> booleanEntry(ConfigEntryBuilder entryBuilder, String key, ForgeConfigSpec.BooleanValue value) {
        return entryBuilder.startBooleanToggle(entryName(key), value.get())
                           .setDefaultValue(value.getDefault())
                           .setTooltip(entryTooltip(key))
                           .setSaveConsumer(value::set)
                           .build();
    }

    /**
     * 效果列表条目：单元格按「效果|持续刻数|等级」校验，不合规时Cloth会拦截保存。
     */
    private static AbstractConfigListEntry<?> stringListEntry(ConfigEntryBuilder entryBuilder, String key, ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
        return entryBuilder.startStrList(entryName(key), new ArrayList<>(value.get()))
                           .setDefaultValue(new ArrayList<>(value.getDefault()))
                           .setCellErrorSupplier(cellValue -> ConfiguredEffect.parse(cellValue).isEmpty()
                                   ? Optional.of(Component.translatable(KEY_PREFIX + key + ".cellError"))
                                   : Optional.empty())
                           .setTooltip(entryTooltip(key))
                           .setSaveConsumer(value::set)
                           .build();
    }
}
