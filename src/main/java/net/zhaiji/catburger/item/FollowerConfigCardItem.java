package net.zhaiji.catburger.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.zhaiji.catburger.capability.FollowerRenderData;
import net.zhaiji.catburger.config.CatBurgerServerConfig;
import net.zhaiji.catburger.config.FollowerConfigMode;
import net.zhaiji.catburger.network.PacketManager;
import net.zhaiji.catburger.network.client.packet.ConfigCardFeedbackPacket;
import net.zhaiji.catburger.network.server.packet.ClientboundFollowerRenderDataPacket;
import net.zhaiji.catburger.util.FollowerConfigCardData;
import net.zhaiji.catburger.util.FollowerConfigCardUtil;
import net.zhaiji.catburger.util.FollowerInteractUtil;
import net.zhaiji.catburger.util.FollowerUtil;
import net.zhaiji.catburger.util.TooltipUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 追随者配置卡：Shift滚轮循环切换添加、移除、穿戴模式；
 * 右键生物按模式写入、移除或戴上追随者，Shift右键按模式复制进卡、清空卡或取下目标头上的追随者。
 */
public class FollowerConfigCardItem extends Item {
    public FollowerConfigCardItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        FollowerConfigMode mode = FollowerConfigCardData.getMode(stack);

        if (player.isShiftKeyDown()) {
            if (mode == FollowerConfigMode.WEAR) return takeOffFollower(level, player, stack);
            return handleCardEdit(level, player, stack, mode);
        }

        LivingEntity target = FollowerInteractUtil.findTarget(player);
        if (target == null) return InteractionResultHolder.fail(stack);

        if (!canApplyTo(player, target)) return consumedResult(level, stack);
        return switch (mode) {
            case ADD -> applyAddMode(level, player, target, stack);
            case REMOVE -> applyRemoveMode(level, player, target, stack);
            case WEAR -> putOnFollower(level, player, target, stack);
        };
    }

    /**
     * 添加模式：把卡片里的配置收束边界后写入目标，并标记该实体不再继承主人的配置。
     */
    private InteractionResultHolder<ItemStack> applyAddMode(Level level, Player player, LivingEntity target, ItemStack stack) {
        FollowerRenderData cardData = FollowerConfigCardData.getConfig(stack);
        if (cardData == null) {
            sendFeedback(player, "empty");
            return consumedResult(level, stack);
        }

        if (!level.isClientSide()) {
            FollowerRenderData targetData = FollowerUtil.followerRenderDataOf(target);

            targetData.deserializeNBT(cardData.serializeNBT());
            targetData.clampToBounds();
            targetData.setManualOverride(true);
            PacketManager.sendToTrackingAndSelf(new ClientboundFollowerRenderDataPacket(target.getUUID(), true, targetData), target);
        }
        sendFeedback(player, "applied");
        return consumedResult(level, stack);
    }

    /**
     * 移除模式：移除目标的配置，同时清掉手动设置标记，让有主生物重新接受主人继承。
     */
    private InteractionResultHolder<ItemStack> applyRemoveMode(Level level, Player player, LivingEntity target, ItemStack stack) {
        if (!level.isClientSide()) {
            FollowerRenderData targetData = FollowerUtil.followerRenderDataOf(target);

            targetData.setManualOverride(false);
            targetData.markUnsynced();
            PacketManager.sendToTrackingAndSelf(new ClientboundFollowerRenderDataPacket(target.getUUID(), false, targetData), target);
        }
        sendFeedback(player, "removed");
        return consumedResult(level, stack);
    }

    /**
     * 非Shift右键的穿戴模式：副手持有追随者时戴到目标头上，原有头盔退回副手。
     */
    private InteractionResultHolder<ItemStack> putOnFollower(Level level, Player player, LivingEntity target, ItemStack stack) {
        ItemStack offhandItem = player.getOffhandItem();
        if (!(offhandItem.getItem() instanceof FollowerItem)) {
            sendFeedback(player, "wear.not_follower");
            return consumedResult(level, stack);
        }

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        if (isLocked(helmet, player)) {
            sendFeedback(player, "wear.locked");
            return consumedResult(level, stack);
        }

        if (!level.isClientSide()) {
            target.setItemSlot(EquipmentSlot.HEAD, offhandItem.copy());
            player.setItemInHand(InteractionHand.OFF_HAND, helmet);
        }
        sendFeedback(player, "wear.applied");
        return consumedResult(level, stack);
    }

    /**
     * 穿戴模式的Shift右键：取下目标头上的追随者，优先放副手，其次物品栏，都满则掉在地上。
     */
    private InteractionResultHolder<ItemStack> takeOffFollower(Level level, Player player, ItemStack stack) {
        LivingEntity target = FollowerInteractUtil.findTarget(player);
        if (target == null) return InteractionResultHolder.fail(stack);

        if (!canApplyTo(player, target)) return consumedResult(level, stack);

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof FollowerItem)) {
            sendFeedback(player, "wear.empty");
            return consumedResult(level, stack);
        }
        if (isLocked(helmet, player)) {
            sendFeedback(player, "wear.locked");
            return consumedResult(level, stack);
        }

        if (!level.isClientSide()) {
            target.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            ItemStack takenHelmet = helmet.copy();
            if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, takenHelmet);
            } else if (!player.getInventory().add(takenHelmet)) {
                player.drop(takenHelmet, false);
            }
        }
        sendFeedback(player, "wear.removed");
        return consumedResult(level, stack);
    }

    /**
     * 绑定诅咒等装备限制在1.20.1由附魔本身表达，创造模式可无视。
     */
    private static boolean isLocked(ItemStack stack, Player player) {
        return !player.isCreative() && EnchantmentHelper.hasBindingCurse(stack);
    }

    /**
     * 按住Shift右键的卡片操作：添加模式复制玩家自己的渲染参数，移除模式清空卡内配置。
     */
    private InteractionResultHolder<ItemStack> handleCardEdit(Level level, Player player, ItemStack stack, FollowerConfigMode mode) {
        if (mode == FollowerConfigMode.ADD) {
            FollowerRenderData ownerData = FollowerUtil.followerRenderDataOf(player);
            if (ownerData == null || !ownerData.isSynced()) {
                sendFeedback(player, "copy_missing");
                return consumedResult(level, stack);
            }
            if (!level.isClientSide()) {
                FollowerConfigCardData.setConfig(stack, ownerData);
            }
            sendFeedback(player, "copied");
        } else {
            if (!level.isClientSide()) {
                FollowerConfigCardData.removeConfig(stack);
            }
            sendFeedback(player, "cleared");
        }

        return consumedResult(level, stack);
    }

    /**
     * 目标是玩家或别人的宠物时拒绝，无主生物允许；提示由服务端回执。
     */
    private static boolean canApplyTo(Player player, LivingEntity target) {
        if (target instanceof Player) {
            sendFeedback(player, "reject.player");
            return false;
        }
        if (target instanceof OwnableEntity ownableEntity) {
            UUID ownerUUID = ownableEntity.getOwnerUUID();
            if (ownerUUID != null && !player.getUUID().equals(ownerUUID)) {
                sendFeedback(player, "reject.owned");
                return false;
            }
        }
        return true;
    }

    /**
     * 操作结果只在服务端产生并回执给客户端，避免客户端预测与服务端判定不一致时显示错误提示。
     */
    private static void sendFeedback(Player player, String messageKey) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketManager.sendToClient(new ConfigCardFeedbackPacket(FollowerConfigCardUtil.message(messageKey)), serverPlayer);
        }
    }

    /**
     * 配置卡的每次右击都算已处理，否则主手判定失败后原版会继续尝试副手物品的交互。
     */
    private static InteractionResultHolder<ItemStack> consumedResult(Level level, ItemStack stack) {
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String keyPrefix = TooltipUtil.itemKeyPrefix(itemStack.getItem());
        FollowerConfigMode mode = FollowerConfigCardData.getMode(itemStack);
        tooltipComponents.add(
                Component.empty()
                         .append(Component.translatable(keyPrefix + "mode"))
                         .append(Component.translatable(keyPrefix + "mode." + mode.getSerializedName()).withStyle(mode.getColor()))
        );

        if (TooltipUtil.appendShiftHint(tooltipComponents, tooltipFlag)) {
            tooltipComponents.addAll(TooltipUtil.cachedEffectLines(this, item -> buildEffectLines(TooltipUtil.itemKeyPrefix(item))));
        }
    }

    /**
     * 按模式分组列出功能，切换模式排在最前；宠物配置同步开启时额外提示卡片设置优先于继承。
     */
    private static List<Component> buildEffectLines(String keyPrefix) {
        List<Component> effectLines = new ArrayList<>();
        effectLines.add(TooltipUtil.effectLine(Component.translatable(keyPrefix + "effect.switch_mode")));
        if (CatBurgerServerConfig.PET_CONFIG_SYNC_ENABLED.get()) {
            effectLines.add(TooltipUtil.effectLine(Component.translatable(keyPrefix + "effect.pet_sync_notice")));
        }
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.ADD, "effect.add.write"));
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.ADD, "effect.add.copy"));
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.REMOVE, "effect.remove.clear"));
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.REMOVE, "effect.remove.reset"));
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.WEAR, "effect.wear.put"));
        effectLines.add(modeEffectLine(keyPrefix, FollowerConfigMode.WEAR, "effect.wear.take"));
        return effectLines;
    }

    /**
     * 前缀与详情分成两个键拼接，颜色只挂在前缀上。
     */
    private static Component modeEffectLine(String keyPrefix, FollowerConfigMode mode, String detailKey) {
        return TooltipUtil.effectLine(Component.empty()
                                             .append(Component.translatable(keyPrefix + "effect.prefix." + mode.getSerializedName()).withStyle(mode.getColor()))
                                             .append(Component.translatable(keyPrefix + detailKey)));
    }
}
