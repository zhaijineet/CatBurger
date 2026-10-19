package net.zhaiji.catburger.config;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 配置里的一条效果定义：效果、持续刻数与等级绑在同一条目上，条目本身就是完整的施加参数。
 */
public record ConfiguredEffect(Holder<MobEffect> effect, int duration, int amplifier) {
    private static final String FIELD_SEPARATOR = "|";

    // 数字字段限定为九位以内十进制数，parseInt既不溢出也不抛异常，因此无需捕获
    private static final Pattern INTEGER_FIELD = Pattern.compile("\\d{1,9}");

    // 等级沿用原版/effect的参数范围，也是MobEffectInstance构造时钳制的上界
    private static final int MAX_AMPLIFIER = 255;

    /**
     * 解析「命名空间:效果id|持续刻数|等级」形式的条目，等级按原版的amplifier计，0为I级。
     */
    public static Optional<ConfiguredEffect> parse(String configuredEffect) {
        String[] fields = configuredEffect.split(Pattern.quote(FIELD_SEPARATOR), -1);
        if (fields.length != 3) return Optional.empty();

        ResourceLocation effectId = ResourceLocation.tryParse(fields[0].trim());
        if (effectId == null) return Optional.empty();

        Optional<Holder.Reference<MobEffect>> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, effectId));
        if (effectHolder.isEmpty()) return Optional.empty();

        String durationField = fields[1].trim();
        String amplifierField = fields[2].trim();
        if (!INTEGER_FIELD.matcher(durationField).matches() || !INTEGER_FIELD.matcher(amplifierField).matches())
            return Optional.empty();

        int duration = Integer.parseInt(durationField);
        int amplifier = Integer.parseInt(amplifierField);
        if (duration <= 0 || amplifier < 0 || amplifier > MAX_AMPLIFIER) return Optional.empty();
        return effectHolder.map(holder -> new ConfiguredEffect(holder, duration, amplifier));
    }

    /**
     * 供配置列表的elementValidator使用：校验器参数类型是Object，非字符串一律判为非法条目。
     */
    public static boolean isValidElement(@Nullable Object element) {
        return element instanceof String configuredEffect && parse(configuredEffect).isPresent();
    }

    /**
     * 解析整张配置列表并丢弃非法条目，使一条写错不会让整张表失效。
     */
    public static List<ConfiguredEffect> resolve(List<? extends String> configuredEffects) {
        return configuredEffects.stream().map(ConfiguredEffect::parse).flatMap(Optional::stream).toList();
    }

    public void applyTo(LivingEntity target) {
        target.addEffect(new MobEffectInstance(effect.value(), duration, amplifier));
    }

    /**
     * 效果名与等级，两者分别取自原版的效果名与药水tooltip等级名，名字表里没有的等级退化为数字。
     */
    public Component displayName() {
        Component effectName = effect.value().getDisplayName();
        if (amplifier <= 0) return effectName;

        return Component.translatable(
                "potion.withAmplifier",
                effectName,
                Component.translatableWithFallback("potion.potency." + amplifier, Integer.toString(amplifier))
        );
    }
}
