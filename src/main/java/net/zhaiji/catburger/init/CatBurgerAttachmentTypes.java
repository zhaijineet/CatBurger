package net.zhaiji.catburger.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.attachment.FollowerRenderData;

import java.util.function.Supplier;

public class CatBurgerAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CatBurger.MOD_ID);

    /**
     * 挂在玩家身上的跟随者渲染参数：落盘以便重生与服务端重启后保留，并自动同步给追踪者。
     */
    public static final Supplier<AttachmentType<FollowerRenderData>> FOLLOWER_RENDER = ATTACHMENT_TYPES.register(
            "follower_render",
            () -> AttachmentType.serializable(FollowerRenderData::new)
                               .copyOnDeath()
                               .sync(FollowerRenderData.STREAM_CODEC)
                               .build()
    );

    /**
     * 配置卡手动设置过的标记，存在即标记；开启宠物配置同步时跳过这些实体，让卡片设置优先于主人继承，随实体落盘并在死亡时复制。
     */
    public static final Supplier<AttachmentType<Unit>> MANUAL_OVERRIDE = ATTACHMENT_TYPES.register(
            "manual_override",
            () -> AttachmentType.builder(() -> Unit.INSTANCE)
                               .serialize(MapCodec.unit(Unit.INSTANCE))
                               .copyOnDeath()
                               .build()
    );
}
